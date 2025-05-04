package sporks

import scala.quoted.*


object Macros {
  // The following method is adapted from Spores3. The original code is licensed
  // under the Apache License, Version 2.0. We have included the original code
  // as a comment for reference.
  // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/Spore.scala
  //
  // -- Start of copy
  // private def checkBodyExpr[T, S](bodyExpr: Expr[T => S])(using Quotes): Unit = {
  //   import quotes.reflect.*
  //
  //   def symIsToplevelObject(sym: Symbol): Boolean =
  //     sym.flags.is(Flags.Module) && sym.owner.flags.is(Flags.Package)
  //
  //   def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
  //     if (sym.maybeOwner.isNoSymbol) false
  //     else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))
  //
  //   def checkCaptures(defdefSym: Symbol, anonfunBody: Tree): Unit = {
  //     /* collect all identifier uses.
  //       check that they don't have an owner outside the anon fun.
  //       uses of top-level objects are OK.
  //     */
  //
  //     val acc = new TreeAccumulator[List[Ident]] {
  //       def foldTree(ids: List[Ident], tree: Tree)(owner: Symbol): List[Ident] = tree match {
  //         case id @ Ident(_) => id :: ids
  //         case _ =>
  //           try {
  //             foldOverTree(ids, tree)(owner)
  //           } catch {
  //             case me: MatchError =>
  //               // compiler bug: skip checking tree
  //               ids
  //           }
  //       }
  //     }
  //     val foundIds = acc.foldTree(List(), anonfunBody)(defdefSym)
  //     val foundSyms = foundIds.map(id => id.symbol)
  //     val names = foundSyms.map(sym => sym.name)
  //     val ownerNames = foundSyms.map(sym => sym.owner.name)
  //
  //     val allOwnersOK = foundSyms.forall(sym =>
  //       ownerChainContains(sym, defdefSym) ||
  //         symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))) // example: `ExecutionContext.Implicits.global`
  //
  //     // report error if not all owners OK
  //     if (!allOwnersOK) {
  //       foundIds.foreach { id =>
  //         val sym = id.symbol
  //         val isOwnedByToplevelObject =
  //           symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))
  //
  //         val isOwnedBySpore = ownerChainContains(sym, defdefSym)
  //         if (!isOwnedByToplevelObject) {
  //           // might find illegal capturing
  //           if (!isOwnedBySpore)
  //             report.error(s"Invalid capture of variable `${id.name}`. Use first parameter of spore's body to refer to the spore's environment.", id.pos)
  //         }
  //       }
  //     }
  //   }
  //
  //   val tree = bodyExpr.asTerm
  //   tree match {
  //     case Inlined(None, List(),
  //       TypeApply(Select(Block(List(), Block(
  //         List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
  //       )), asInst), _)
  //     ) =>
  //       checkCaptures(defdef.symbol, anonfunBody)
  //
  //     case Inlined(None, List(),
  //       TypeApply(Select(Block(
  //         List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
  //       ), asInst), _)
  //     ) =>
  //       checkCaptures(defdef.symbol, anonfunBody)
  //
  //     case Inlined(None, List(),
  //       Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _))) =>
  //       checkCaptures(defdef.symbol, anonfunBody)
  //
  //     case Inlined(None, List(), Block(List(),
  //       Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)))) =>
  //       checkCaptures(defdef.symbol, anonfunBody)
  //
  //     case _ =>
  //       val str = tree.show(using Printer.TreeStructure)
  //       report.error(s"Argument must be a function literal", tree.pos)
  //   }
  // }
  // -- End of copy

  private[sporks] def checkBodyExpr[T](bodyExpr: Expr[T])(using Quotes): Unit = {
    import quotes.reflect.*

    def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
      if (sym.maybeOwner.isNoSymbol) false
      else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))

    def checkCaptures(defdefSym: Symbol, anonfunBody: Tree): Unit = {

      val acc = new TreeAccumulator[List[Tree]] {
        def foldTree(ids: List[Tree], tree: Tree)(owner: Symbol): List[Tree] = tree match {
          case id @ Ident(_) =>
            // Ignore id if is a type
            if id.symbol.isType then ids
            else id :: ids
          // Special case for `this`
          case thiz @ This(_) => thiz :: ids
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

      foundIds.foreach(id =>
        id match
          case This(opt) =>
            report.error(s"Invalid capture of `this` from class ${opt}.", id.pos)
          case _ => ()
      )

      def symIsToplevelObject(sym: Symbol): Boolean =
        sym.isNoSymbol ||
          (
            (sym.flags.is(Flags.Module) || sym.flags.is(Flags.Package))
              && symIsToplevelObject(sym.owner)
          )

      def isOwnedByToplevelObject(sym: Symbol): Boolean =
        symIsToplevelObject(sym)
          || (!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)
        // Here we don't do the following check as is done in Spores3:
        //   || (
        //        (!sym.maybeOwner.isNoSymbol)
        //          && (!sym.owner.maybeOwner.isNoSymbol)
        //          && symIsToplevelObject(sym.owner.owner)
        //      )
        // ... otherwise the following case passes:
        //   object Foo { def bar(x: Int) = Spork.apply[Int => Int] { y => x + y } }
        // ... which captures `x`, and cause a runtime exception when the spork
        // is built (when Foo is top-level non-nested object).
        // PH noted above: "// example: `ExecutionContext.Implicits.global`" is
        // not allowed to be captured if commented out. Well... better safe than
        // sorry.

      def isOwnedBySpore(sym: Symbol): Boolean =
        ownerChainContains(sym, defdefSym)

      foundIds.foreach { id =>
        val sym = id.symbol
        if (!isOwnedByToplevelObject(sym)) {
          if (!isOwnedBySpore(sym))
            report.error(s"Invalid capture of variable `${id.symbol.name}`. Use the first parameter of a spork's body to refer to the spork's environment.", id.pos)
        }
      }
    }

    def checkCapturesTree(tree: Term): Unit = {
      // TODO: Refactor this to method to avoid duplication and co-dependency
      // with the above `findCapturesTree` method.
      tree match {
        case Inlined(_, _, TypeApply(Select(Block(_, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _)), _), _)) =>
          checkCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, TypeApply(Select(Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _), _), _)) =>
          checkCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _)) =>
          checkCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, Block(_, Block(List(defdef @ DefDef(_, _, _, Some(anonfunBody))), _))) =>
          checkCaptures(defdef.symbol, anonfunBody)
        case Inlined(_, _, nested@ Inlined(_, _, _)) =>
          checkCapturesTree(nested)
        case _ =>
          checkCaptures(tree.symbol, tree)
      }
    }

    val tree = bodyExpr.asTerm
    checkCapturesTree(tree)
  }

  private[sporks] def isTopLevelObject[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be "static", i.e., top-level or defined inside a static object

    def isObject(sym: Symbol): Boolean =
      sym.flags.is(Flags.Module)

    // The following method is adapted from Spores3. The original code is licensed
    // under the Apache License, Version 2.0. We have included the original code
    // as a comment for reference.
    // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/Spore.scala
    //
    // -- Start of copy
    // def allOwnersOK(owner: Symbol): Boolean =
    //   owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))
    //
    // val tree       = builderExpr.asTerm
    // val builderTpe = tree.tpe
    // val owner      = builderTpe.typeSymbol.maybeOwner
    // if (!allOwnersOK(owner)) {
    //   report.error("An owner of the provided builder is neither an object nor a package.")
    // }
    //
    // val fn = Expr(tree.show)
    // '{
    //   new SporeData[T, R]($fn) {
    //     type Env = N
    //     def envOpt = $envOptExpr
    //   }
    // }
    // -- End of copy

    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner = builderTpe.typeSymbol.maybeOwner

    if (!isObject(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkObjectBuilder `${builderTpe.typeSymbol.fullName}` is not an object.")
    }
    if (!allOwnersOK(owner)) {
      report.error(s"The provided SporkObjectBuilder `${builderTpe.typeSymbol.fullName}` is not a top-level object; its owner `${owner.name}` is not a top-level object nor a package.")
    }

    '{ () }
  }

  private[sporks] def isTopLevelClass[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be concrete
    // > It must have at least one public constructor
    // > It must not be a local class, i.e., defined inside a method
    //
    // In addition, we do the following checks.
    // > It is not nested in another class.
    // > It has a constructor with an empty parameter list.
    // > It has no context parameters in its parameter lists.

    def isClass(sym: Symbol): Boolean =
      sym.isClassDef && !sym.flags.is(Flags.Module)

    def isConcrete(sym: Symbol): Boolean = {
      !sym.flags.is(Flags.Abstract)
      && !sym.flags.is(Flags.Trait)
      && !sym.flags.is(Flags.Sealed)
    }

    def isPublic(sym: Symbol): Boolean = {
      !sym.flags.is(Flags.Private)
      && !sym.flags.is(Flags.Protected)
    }

    def isNotLocal(owner: Symbol): Boolean =
      owner.isNoSymbol || (!owner.flags.is(Flags.Method) && isNotLocal(owner.owner))

    def isNotNestedInClass(owner: Symbol): Boolean =
      owner.isNoSymbol || (!(owner.isClassDef && !owner.flags.is(Flags.Module)) && isNotNestedInClass(owner.owner))

    def containsEmptyParamList(sym: Symbol): Boolean = {
      sym.paramSymss.isEmpty
      || sym.paramSymss.exists(_.isEmpty)
    }

    def containsContextParamList(sym: Symbol): Boolean = {
      sym.paramSymss.exists(_.exists(x => x.flags.is(Flags.Implicit) || x.flags.is(Flags.Given)))
    }

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner = builderTpe.typeSymbol.maybeOwner

    if (!isClass(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` is not a class.")
    }
    if (!isConcrete(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` is not a concrete class.")
    }
    val constructor = builderTpe.typeSymbol.primaryConstructor
    if (!isPublic(constructor)) {
      report.error(s"The provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` `${constructor.name}` does not have a public constructor.")
    }
    if (!isNotLocal(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` is not a local class.")
    }
    if (!isNotNestedInClass(builderTpe.typeSymbol.owner)) {
      report.error(s"The provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` is nested in a class.")
    }
    if (!containsEmptyParamList(constructor)) {
      report.error(s"The constructor of the provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` `${constructor.name}` does not have an empty parameter list.")
    }
    if (containsContextParamList(constructor)) {
      report.error(s"The constructor of the provided SporkClassBuilder `${builderTpe.typeSymbol.fullName}` `${constructor.name}` contains a context parameter list.")
    }

    '{ () }
  }
}
