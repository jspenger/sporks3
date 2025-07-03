package sporks

import scala.quoted.*

private[sporks] object Macros {

  private[sporks] def findCapturedIds(using Quotes)(tree: quotes.reflect.Term): List[quotes.reflect.Tree] = {
    import quotes.reflect.*

    def findMaybeOwners(tree: Tree): List[Symbol] = {
      // Collect all symbols which are maybeOwners of other symbols in the tree.
      // This includes:
      // - ValDef
      // - DefDef
      // - ClassDef
      // - TypeDef

      val acc = new TreeAccumulator[List[Symbol]] {
        def foldTree(owners: List[Symbol], tree: Tree)(owner: Symbol): List[Symbol] = tree match {
          case x @ ValDef(_, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ DefDef(_, _, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ ClassDef(_, _, _, _, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case x @ TypeDef(_, _) =>
            foldOverTree(x.symbol :: owners, tree)(owner)
          case _ =>
            foldOverTree(owners, tree)(owner)
        }
      }
      acc.foldTree(List(), tree)(tree.symbol)
    }

    def findCaptures(anonfunBody: Tree): List[Tree] = {

      def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean = {
        if (sym.maybeOwner.isNoSymbol) false else ((sym.maybeOwner == transitiveOwner) || ownerChainContains(sym.maybeOwner, transitiveOwner))
      }

      def symIsToplevelObject(sym: Symbol): Boolean = {
        sym.isNoSymbol || ((sym.flags.is(Flags.Module) || sym.flags.is(Flags.Package)) && symIsToplevelObject(sym.maybeOwner))
      }

      def isOwnedByToplevelObject(sym: Symbol): Boolean = {
        symIsToplevelObject(sym) || (!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.maybeOwner)
      }

      // A symbol is owned by the spore if:
      // - it is owned by one of the maybeOwners of the spore
      // - it is a one of the maybeOwners of the spore

      val maybeOwners = findMaybeOwners(anonfunBody)
      def isOwnedBySpore(sym: Symbol): Boolean = {
        maybeOwners.exists(ownerChainContains(sym, _)) || maybeOwners.contains(sym)
      }

      def isThis(tree: Tree): Boolean = {
        tree match {
          case This(_) => true
          case _ => false
        }
      }

      // Collect all identifiers which *could* be captured by the closure. This
      // will also collect identifiers which are not captured by the closure
      // such as identifiers owned by toplevel objects, etc. These are later
      // filtered out.
      // This includes:
      // - Ident
      // - This
      // - TypeIdent

      def outermostAppliedTypeIdent(tree: Tree): Option[Tree] = {
        tree match {
          case x @ TypeIdent(_) => Some(x)
          case Applied(x @ _, _) => outermostAppliedTypeIdent(x)
          case _ => None
        }
      }

      val acc = new TreeAccumulator[List[Tree]] {
        def foldTree(ids: List[Tree], tree: Tree)(owner: Symbol): List[Tree] = tree match {
          case x @ Ident(_) if !x.symbol.isType =>
            x :: ids
          case x @ This(_) =>
            x :: ids
          // `TypeIdent`s are tricky... we want to find any occurrence of a
          // TypeIdent `T` in either of these cases: 
          // - `new T`
          // - `new T[U]`
          // - `class C extends T with ...`
          // - `class C extends T[U] with ...`
          // - `class C extends ... with T`
          // - `class C extends ... with T[U]`
          case ClassDef(_, _, parents, _, _) =>
            // `parents`` are either `TypeTree`s or `Term` containing `New`.
            // Here we collect all `TypeIdent`s from `TypeTree`s. 
            // The `folderOverTree` call collects the `TypeIdent`s from `New`.
            parents.flatMap { outermostAppliedTypeIdent(_) }
            ::: foldOverTree(ids, tree)(owner)
          case New(x) =>
            outermostAppliedTypeIdent(x).toList
            ::: foldOverTree(ids, tree)(owner)
          case _ =>
            foldOverTree(ids, tree)(owner)
        }
      }

      val foundIds = acc.foldTree(List(), anonfunBody)(anonfunBody.symbol.maybeOwner)

      // Filter out identifiers which are either:
      // - owned by a toplevel symbol or are a toplevel symbol
      // - owned by the spore
      foundIds
        .filter(tree => isThis(tree) || !isOwnedByToplevelObject(tree.symbol))
        .filter(tree => !isOwnedBySpore(tree.symbol))
    }

    findCaptures(tree)
  }


  private[sporks] def checkBodyExpr[T](bodyExpr: Expr[T])(using Quotes): Unit = {
    import quotes.reflect.*

    val foundIds = findCapturedIds(bodyExpr.asTerm)

    foundIds.foreach { tree => 
      tree match {
        case This(Some(qual)) => {
          report.error(s"Invalid capture of `this` from class ${qual}.", tree.pos)
        }
        case This(None) => {
          report.error(s"Invalid capture of `this` from outer class.", tree.pos)
        }
        case _ => {
          val sym = tree.symbol
          report.error(s"Invalid capture of variable `${sym.name}`. Use the first parameter of a spork's body to refer to the spork's environment.", tree.pos)
        }
      }
    }
  }


  private[sporks] def isTopLevelObject[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be "static", i.e., top-level or defined inside a static object

    def isObject(sym: Symbol): Boolean = {
      sym.flags.is(Flags.Module)
    }

    def allOwnersOK(owner: Symbol): Boolean = {
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.maybeOwner))
    }

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val sym = builderTpe.typeSymbol
    val owner = sym.maybeOwner

    if (!isObject(sym)) {
      report.error(s"The provided SporkBuilder `${sym.fullName}` is not an object.")
    }
    if (!allOwnersOK(owner)) {
      report.error(s"The provided SporkBuilder `${sym.fullName}` is not a top-level object; its owner `${owner.name}` is not a top-level object nor a package.")
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

    def isClass(sym: Symbol): Boolean = {
      sym.isClassDef && !sym.flags.is(Flags.Module)
    }

    def isConcrete(sym: Symbol): Boolean = {
      !sym.flags.is(Flags.Abstract)
      && !sym.flags.is(Flags.Trait)
      && !sym.flags.is(Flags.Sealed)
    }

    def isPublic(sym: Symbol): Boolean = {
      !sym.flags.is(Flags.Private)
      && !sym.flags.is(Flags.Protected)
    }

    def isNotLocal(owner: Symbol): Boolean = {
      owner.isNoSymbol || (!owner.flags.is(Flags.Method) && isNotLocal(owner.maybeOwner))
    }

    def isNotNestedInClass(owner: Symbol): Boolean = {
      owner.isNoSymbol || (!(owner.isClassDef && !owner.flags.is(Flags.Module)) && isNotNestedInClass(owner.maybeOwner))
    }

    def containsEmptyParamList(sym: Symbol): Boolean = {
      sym.paramSymss.isEmpty || sym.paramSymss.exists(_.isEmpty)
    }

    def containsContextParamList(sym: Symbol): Boolean = {
      sym.paramSymss.exists(_.exists(x => x.flags.is(Flags.Implicit) || x.flags.is(Flags.Given)))
    }

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val sym = builderTpe.typeSymbol

    if (!isClass(sym)) {
      report.error(s"The provided SporkClassBuilder `${sym.fullName}` is not a class.")
    }
    if (!isConcrete(sym)) {
      report.error(s"The provided SporkClassBuilder `${sym.fullName}` is not a concrete class.")
    }
    val constructor = sym.primaryConstructor
    if (!isPublic(constructor)) {
      report.error(s"The provided SporkClassBuilder `${sym.fullName}` `${constructor.name}` does not have a public constructor.")
    }
    if (!isNotLocal(sym)) {
      report.error(s"The provided SporkClassBuilder `${sym.fullName}` is a local class.")
    }
    if (!isNotNestedInClass(sym.maybeOwner)) {
      report.error(s"The provided SporkClassBuilder `${sym.fullName}` is nested in a class.")
    }
    if (!containsEmptyParamList(constructor)) {
      report.error(s"The constructor of the provided SporkClassBuilder `${sym.fullName}` `${constructor.name}` does not have an empty parameter list.")
    }
    if (containsContextParamList(constructor)) {
      report.error(s"The constructor of the provided SporkClassBuilder `${sym.fullName}` `${constructor.name}` contains a context parameter list.")
    }

    '{ () }
  }
}


// This file contains code adapted from Spores 3. The original code is licensed
// under the Apache License, Version 2.0. We have included the original code
// as a comment for reference.
//
// Methods which contain adapted code:
// - findCapturedIds
// - checkBodyExpr
//
// The original code is included as a comment below for reference.
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


// This file contains code adapted from Spores 3. The original code is licensed
// under the Apache License, Version 2.0. We have included the original code
// as a comment for reference.
//
// Methods which contain adapted code:
// - isTopLevelObject
//
// The original code is included as a comment below for reference.
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
