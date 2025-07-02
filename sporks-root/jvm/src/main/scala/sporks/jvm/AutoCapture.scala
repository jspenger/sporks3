package sporks.jvm

import scala.quoted.*
import upickle.default.ReadWriter

import sporks.*
import sporks.given


/** A factory for creating Sporks that are safe to serialize and deserialize.
  * Automatically captures variables and checks captured variables.
  *
  * Automatically capturing a variable requires an implicit
  * `Spork[ReadWriter[T]]` in scope, where `T` is the type of the captured
  * variable.
  *
  * Note: This Spork factory only works on the JVM. Use the
  * [[sporks.SporkBuilder]] or [[sporks.SporkClassBuilder]] if ScalaJS or
  * ScalaNative support is needed.
  */
object AutoCapture {

  /** Create a Spork from `f`. Captured variables in `f` must have an implicit
    * `Spork[ReadWriter[T]]` in scope, where `T` is the type of the captured
    * variable.
    *
    * The created Spork is safe to serialize and deserialize. If a captured
    * variable does not have an implicit `Spork[ReadWriter[T]]` in scope then it
    * will cause a compiler error.
    *
    * @example
    *   {{{
    * def isBetween(x: Int , y: Int): Spork[Int => Boolean] = {
    *   // `x` and `y` are captured variables of type `Int`, so we need to
    *   // provide an implicit `Spork[ReadWriter[Int]]` in scope.
    *   AutoCapture.apply { (i: Int) => x <= i && i < y }
    * }
    *   }}}
    *
    * @param f
    *   The closure.
    * @tparam F
    *   The type of the closure.
    * @return
    *   A new `Spork[F]` with the packed closure `f`.
    */
  inline def apply[F](inline f: F): Spork[F] = {
    ${ liftImpl[F]('f) }
  }

  private def liftImpl[F: Type](fExpr: Expr[F])(using Quotes): Expr[Spork[F]] = {
    import quotes.reflect.*

    // 1. Get all captured symbols
    // Captures are grouped into a list of groups, where each group contains all
    // symbols which share the same captured identifier. One environment
    // parameter is generated for each group.
    val foundIds = Macros.findCapturedIds(fExpr.asTerm)
    val captures: List[List[Tree]] = foundIds.groupBy(_.symbol.fullName).toList.sortBy(_._1).map(_._2).toList

    // 2. Check if every capture has a ReadWriter
    val readWriters = captures.flatMap { cap =>
      // I'm unsure if captures from the same group ever have different types.
      // In the odd case they do, we can do the following instead:
      // ((alternative: )) val capTpe = cap.map(_.symbol.termRef.widen).reduceLeft(AndType(_, _))
      // `cap.head` is safe as `cap` groups are nonempty by construction
      val capTpe = cap.head.symbol.termRef.widen
      val rwTpe = TypeRepr.of[[T] =>> Spork[ReadWriter[T]]].appliedTo(capTpe)
      val result = Implicits.search(rwTpe)
      result match {
        case succ: ImplicitSearchSuccess =>
          List(succ.tree.asExpr)
        case fail: ImplicitSearchFailure =>
          cap.foreach { c =>
            // Do not use `rwTpe.show` here as it throws a MatchError on Scala 3.4.3 for some cases
            val msg = s"Missing implicit for captured variable `${c.symbol.name}`.\n\n" + fail.explanation
            report.error(msg, c.pos)
          }
          List()
      }
    }

    // 3. Lift each group of captured symbols, one group at a time
    def liftSymbolGroup(owner: Symbol, sym: List[Symbol], body: Term): Term = {
      val mtpe = MethodType(List(sym.head.name))(_ => List(sym.head.termRef), _ => body.tpe)
      Lambda(
        owner,
        mtpe,
        { case (methSym, List(arg1: Term)) =>
            val subst = sym.map(s => (s -> arg1)).toMap
            val treeMap = new TreeMap {
              override def transformTerm(t: Term)(o: Symbol): Term = t match {
                case id: Ident =>
                  subst.getOrElse(id.symbol, super.transformTerm(t)(o))
                case _ =>
                  super.transformTerm(t)(o)
              }
            }
            treeMap.transformTerm(body)(methSym).changeOwner(methSym)
          case _ => ??? // TODO: throw sensible error
        }
      )
    }

    def liftAllSymbolGroups(owner: Symbol, syms: List[List[Tree]], body: Term): Term = {
      var newBody = body
      var newOwner = owner
      for (sym <- syms) do {
        newBody = liftSymbolGroup(newOwner, sym.map(_.symbol), newBody)
        newOwner = newBody.symbol
      }
      newBody
    }

    val lifted = liftAllSymbolGroups(Symbol.spliceOwner, captures.reverse, fExpr.asTerm)

    // 4. Pack the new lifted function...
    val packed: Expr[Spork[Any]] = '{
      val lambda = {
        class Lambda extends SporkLambdaBuilder(${lifted.asExpr})
        (new Lambda())
      }
      lambda.pack()
    }

    // ... and pack it with the captures and readWriters
    var tmp = packed
    for ((cap, rw) <- captures zip readWriters) do {
      val env: Expr[Any] = Ref(cap.head.symbol).asExpr
      tmp = '{
        $tmp
          .asInstanceOf[Spork[Any => Any]]
          .withEnv($env)(using $rw.asInstanceOf)
      }
    }

    '{
      $tmp.asInstanceOf[Spork[F]]
    }
  }

}
