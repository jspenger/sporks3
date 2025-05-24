package sporks.jvm

import scala.quoted.*
import upickle.default.ReadWriter

import sporks.*
import sporks.given


object AutoCapture {

  inline def apply[F](inline f: F): Spork[F] = {
    ${ liftImpl[F]('f) }
  }

  private def liftImpl[F: Type](fExpr: Expr[F])(using Quotes): Expr[Spork[F]] = {
    import quotes.reflect.*

    // 1. Get all captured symbols
    val foundIds = Macros.findCapturedIds(fExpr.asTerm)
    val captures = foundIds.map(_.symbol).distinct.sorted(Ordering.by(_.fullName))

    // 2. Check if every capture has a ReadWriter
    val readWriters = captures.flatMap { cap =>
      val capTpe = cap.termRef.widen
      val rwTpe = TypeRepr.of[[T] =>> Spork[ReadWriter[T]]].appliedTo(capTpe)
      val result = Implicits.search(rwTpe)
      result match {
        case succ: ImplicitSearchSuccess =>
          List(succ.tree.asExpr)
        case fail: ImplicitSearchFailure =>
          report.error(fail.explanation)
          List()
      }
    }

    // 3. Lift each captured symbol, one at a time
    def liftSymbol(owner: Symbol, sym: Symbol, body: Term): Term = {
      val mtpe = MethodType(List(sym.name))(_ => List(sym.termRef), _ => body.tpe)
      Lambda(
        owner,
        mtpe,
        { case (methSym, List(arg1: Term)) =>
            val subst = Map(sym -> arg1)
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

    def liftAllSymbols(owner: Symbol, syms: List[Symbol], body: Term): Term = {
      var newBody = body
      var newOwner = owner
      for (sym <- syms) do {
        newBody = liftSymbol(newOwner, sym, newBody)
        newOwner = newBody.symbol
      }
      newBody
    }

    val lifted = liftAllSymbols(Symbol.spliceOwner, captures.reverse, fExpr.asTerm)

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
      val env: Expr[Any] = Ref(cap).asExpr
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
