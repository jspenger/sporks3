package sporks

import sporks.*
import scala.quoted.*

object Macros {
  // Copied from Spores3
  // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/Spore.scala
  // JS: Made it private[sporks] instead of private
  private[sporks] def checkBodyExpr[T](bodyExpr: Expr[T])(using Quotes): Unit = {
    import quotes.reflect.*

    def symIsToplevelObject(sym: Symbol): Boolean =
      sym.flags.is(Flags.Module) && sym.owner.flags.is(Flags.Package)

    def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
      if (sym.maybeOwner.isNoSymbol) false
      else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))

    def checkCaptures(defdefSym: Symbol, anonfunBody: Tree): Unit = {
      /* collect all identifier uses.
        check that they don't have an owner outside the anon fun.
        uses of top-level objects are OK.
      */

      val acc = new TreeAccumulator[List[Ident]] {
        def foldTree(ids: List[Ident], tree: Tree)(owner: Symbol): List[Ident] = tree match {
          case id @ Ident(_) => id :: ids
          case _ =>
            try {
              foldOverTree(ids, tree)(owner)
            } catch {
              case me: MatchError =>
                // compiler bug: skip checking tree
                ids
            }
        }
      }
      val foundIds = acc.foldTree(List(), anonfunBody)(defdefSym)
      val foundSyms = foundIds.map(id => id.symbol)
      val names = foundSyms.map(sym => sym.name)
      val ownerNames = foundSyms.map(sym => sym.owner.name)

      val allOwnersOK = foundSyms.forall(sym =>
        ownerChainContains(sym, defdefSym) ||
          symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(
            sym.owner
          )) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))
      ) // example: `ExecutionContext.Implicits.global`

      // report error if not all owners OK
      if (!allOwnersOK) {
        foundIds.foreach { id =>
          val sym = id.symbol
          val isOwnedByToplevelObject =
            symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(
              sym.owner
            )) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))

          val isOwnedBySpore = ownerChainContains(sym, defdefSym)
          if (!isOwnedByToplevelObject) {
            // might find illegal capturing
            if (!isOwnedBySpore)
              report.error(s"Invalid capture of variable `${id.name}`. Use the first parameter of a spork's body to refer to the spork's environment.", id.pos)
          }
        }
      }
    }

    val tree = bodyExpr.asTerm
    tree match {
      case Inlined(
            None,
            List(),
            TypeApply(
              Select(
                Block(
                  List(),
                  Block(
                    List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))),
                    Closure(_, _)
                  )
                ),
                asInst
              ),
              _
            )
          ) =>
        checkCaptures(defdef.symbol, anonfunBody)

      case Inlined(
            None,
            List(),
            TypeApply(
              Select(
                Block(
                  List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))),
                  Closure(_, _)
                ),
                asInst
              ),
              _
            )
          ) =>
        checkCaptures(defdef.symbol, anonfunBody)

      case Inlined(None, List(), Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _))) =>
        checkCaptures(defdef.symbol, anonfunBody)

      case Inlined(None, List(), Block(List(), Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)))) =>
        checkCaptures(defdef.symbol, anonfunBody)

      case _ =>
        // JS: commented out, it is ok if the body is not a lambda
        // val str = tree.show(using Printer.TreeStructure)
        // report.error(s"Argument must be a function literal", tree.pos)
        // JS: instead we check captures for the body
        checkCaptures(tree.symbol, tree)
    }
  }
  // End of copied code

  private[sporks] def checkOwners[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Copied from Spores3
    // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/SporeData.scala
    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner = builderTpe.typeSymbol.maybeOwner
    if (!allOwnersOK(owner)) {
      report.error("An owner of the provided builder is neither an object nor a package.")
    }
    // End of copied code

    '{ () }
  }

}
