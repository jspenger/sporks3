package sporks.experimental.jvm

import scala.quoted.*

import sporks.*
import sporks.given


object Lift {

  private type Codec[T] = PackedSpork[upickle.default.ReadWriter[T]]

  inline def apply[F](inline f: F): PackedSpork[F] = {
    ${ liftImpl[F]('f) }
  }

  private[sporks] def liftImpl[F: Type](fExpr: Expr[F])(using Quotes): Expr[PackedSpork[F]] = {
    import quotes.reflect.*

    // 1. Get all captured symbols
    val captures = capturedSymbols(fExpr)

    if (captures.isEmpty) then {
      return
        '{
          val lambda = {
            class Lambda extends SporkLambdaBuilder($fExpr)
            (new Lambda())
          }
          lambda.pack()
        }.asExprOf[PackedSpork[F]]
    }

    // 2. Check if every capture has a Codec
    val codecs = captures.flatMap { cap =>
      val capTpe = cap.termRef.widen
      val codTpe = TypeRepr.of[Codec].appliedTo(capTpe)
      val result = Implicits.search(codTpe)
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

    // 4. Pack the new lifted function and pack it with the captured symbols and codecs
    val packed: Expr[PackedSpork[Any]] = '{
      val lambda = {
        class Lambda extends SporkLambdaBuilder(${lifted.asExpr})
        (new Lambda())
      }
      lambda.pack()
    }

    var tmp = packed
    for ((cap, codec) <- captures zip codecs) do {
      val env = Ref(cap).asExpr
      val cod = codec
      tmp = '{
        $tmp
          .asInstanceOf[PackedSpork[Any => Any]]
          .withEnv($env)(using $cod.asInstanceOf)
      }
    }
    '{
      $tmp.asInstanceOf[PackedSpork[F]]
    }
  }


  private[sporks] def capturedSymbols[T](bodyExpr: Expr[T])(using Quotes): List[quotes.reflect.Symbol] = {
    import quotes.reflect.*

    def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
      if (sym.maybeOwner.isNoSymbol) false
      else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))

    def findCaptures(defdefSym: Symbol, anonfunBody: Tree): List[Symbol] = {
      val acc = new TreeAccumulator[List[Tree]] {
        def foldTree(ids: List[Tree], tree: Tree)(owner: Symbol): List[Tree] = tree match {
          case id @ Ident(_) if !id.symbol.isType => id :: ids
          case thiz @ This(_) => thiz :: ids
          case _ => try {
            foldOverTree(ids, tree)(owner)
          } catch {
            case me: MatchError => {
            // compiler bug: skip checking tree
            ids
            }
          }
        }
      }
      val foundIds = acc.foldTree(List(), anonfunBody)(defdefSym)

      def symIsToplevelObject(sym: Symbol): Boolean =
        sym.isNoSymbol ||
          (
            (sym.flags.is(Flags.Module) || sym.flags.is(Flags.Package))
              && symIsToplevelObject(sym.owner)
          )

      def isOwnedByToplevelObject(sym: Symbol): Boolean =
        symIsToplevelObject(sym)
          || (!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)

      def isOwnedBySpore(sym: Symbol): Boolean =
        ownerChainContains(sym, defdefSym)

      foundIds
        .map(_.symbol)
        .filter(sym => !isOwnedByToplevelObject(sym))
        .filter(sym => !isOwnedBySpore(sym))
        .distinct
        .sorted(Ordering.by(_.fullName))
    }

    def findCapturesTree(tree: Term): List[Symbol] = {
      // This is a copy-and-paste of the code in Macros.scala, replacing
      // `checkCaptures` with `findCaptures`, and `checkCapturesTree` with
      // `findCapturesTree`. If one is edited, make sure to edit the other,
      // or, better, refactor this into a common function.
      tree match {
        case Inlined(_, _, TypeApply(Select(Block(_, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _)), _), _)) =>
          findCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, TypeApply(Select(Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _), _), _)) =>
          findCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _)) =>
          findCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, Block(_, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _))) =>
          findCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, nested@ Inlined(_, _, _)) =>
          findCapturesTree(nested)
        case _ =>
          findCaptures(tree.symbol, tree)
      }
    }

    val tree = bodyExpr.asTerm
    findCapturesTree(tree)
  }
}
