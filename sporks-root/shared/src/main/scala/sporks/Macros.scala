package sporks

import sporks.*
import scala.quoted.*

object Macros {
  // Copied from Spores3
  // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/Spore.scala
  // JS: Made it private[sporks] instead of private
  private[sporks] def checkBodyExpr[T](bodyExpr: Expr[T])(using Quotes): Unit = {
    import quotes.reflect.*

    // JS: Commented out
    // def symIsToplevelObject(sym: Symbol): Boolean =
    //   sym.flags.is(Flags.Module) && sym.owner.flags.is(Flags.Package)

    def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
      if (sym.maybeOwner.isNoSymbol) false
      else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))

    def checkCaptures(defdefSym: Symbol, anonfunBody: Tree): Unit = {
      /* collect all identifier uses.
        check that they don't have an owner outside the anon fun.
        uses of top-level objects are OK.
      */

      // JS: changed Ident -> Tree
      val acc = new TreeAccumulator[List[Tree]] {
        def foldTree(ids: List[Tree], tree: Tree)(owner: Symbol): List[Tree] = tree match {
          case id @ Ident(_) => 
            // JS: ignore id if is type
            if id.symbol.isType then ids
            else id :: ids
          // JS: added special case for `this`
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
      // JS: Commented out as not used
      // val foundSyms = foundIds.map(id => id.symbol)
      // val names = foundSyms.map(sym => sym.name)
      // // JS: added guard to not check if owner is NoSymbol as otherwise it may
      // // throw an NoDenotation.owner exception.
      // val ownerNames = foundSyms.flatMap(sym => 
      //   if !sym.maybeOwner.isNoSymbol then List(sym.owner.name) else List.empty
      // )

      // JS: add check and report error if captured `this`
      foundIds.foreach(id => id match
        case This(opt) =>
          report.error(s"Invalid capture of `this` from class ${opt}.", id.pos)
        case _ => ()
      )

      // JS: reorganize the following code to reduce repetition and format for readability
      // val allOwnersOK = foundSyms.forall(sym =>
      //   ownerChainContains(sym, defdefSym) ||
      //     symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(
      //       sym.owner
      //     )) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))
      // ) // example: `ExecutionContext.Implicits.global`

      // // report error if not all owners OK
      // if (!allOwnersOK) {
      //   foundIds.foreach { id =>
      //     val sym = id.symbol
      //     val isOwnedByToplevelObject =
      //       symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(
      //         sym.owner
      //       )) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))

      //     val isOwnedBySpore = ownerChainContains(sym, defdefSym)
      //     if (!isOwnedByToplevelObject) {
      //       // might find illegal capturing
      //       if (!isOwnedBySpore)
      //         // JS: id -> id.symbol
      //         report.error(s"Invalid capture of variable `${id.symbol.name}`. Use the first parameter of a spork's body to refer to the spork's environment.", id.pos)
      //     }

      // JS: New method to check if symbol is top-level or is object nested in top-level object
      def symIsToplevelObject(sym: Symbol): Boolean =
          sym.isNoSymbol ||
          (
            (sym.flags.is(Flags.Module) || sym.flags.is(Flags.Package))
            && symIsToplevelObject(sym.owner)
          )

      def isOwnedByToplevelObject(sym: Symbol): Boolean =
        symIsToplevelObject(sym)
        || (!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)
        // JS: commented out... otherwise the following case passes:
        //     object Foo { def bar(x: Int) = Spork.apply[Int => Int] { y => x + y } }
        //     ...which will capture `x`, and cause a runtime exception when the
        //     spork is built (when Foo is top-level non-nested object).
        //     As PH noted: "// example: `ExecutionContext.Implicits.global`" is
        //     not allowed to be captured if commented out. Perhaps it is 
        //     acceptable. The compiler does not always capture contextual
        //     global givens, but it may do so in some cases, which would end in
        //     a runtime exception.
        // ||  (
        //       (!sym.maybeOwner.isNoSymbol)
        //       && (!sym.owner.maybeOwner.isNoSymbol)
        //       && symIsToplevelObject(sym.owner.owner)
        //     )
      
      def isOwnedBySpore(sym: Symbol): Boolean =
        ownerChainContains(sym, defdefSym)

      foundIds.foreach { id =>
        val sym = id.symbol
        if (!isOwnedByToplevelObject(sym)) {
          if (!isOwnedBySpore(sym))
            // JS: id -> id.symbol
            report.error(s"Invalid capture of variable `${id.symbol.name}`. Use the first parameter of a spork's body to refer to the spork's environment.", id.pos)
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

  private[sporks] def isTopLevelObject[T](builderExpr: Expr[T])(using Type[T], Quotes): Expr[Unit] = {
    import quotes.reflect.*

    // Here we check the following to ensure that it works on Scala.js and Scala Native:
    // See: https://github.com/portable-scala/portable-scala-reflect
    // > It must be "static", i.e., top-level or defined inside a static object

    def isObject(sym: Symbol): Boolean =
      sym.flags.is(Flags.Module)

    // Copied from Spores3
    // See: https://github.com/phaller/spores3/blob/main/shared/src/main/scala/com/phaller/spores/SporeData.scala
    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    // JS: add check for object
    if (!isObject(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkObject `${builderExpr.show}` is not an object.")
    }
    // End JS
    val owner = builderTpe.typeSymbol.maybeOwner
    if (!allOwnersOK(owner)) {
      // JS: commented out
      // report.error("An owner of the provided builder is neither an object nor a package.")
      // JS: instead we also report the builderExpr and the name of the owner
      report.error(s"The provided SporkObject `${builderExpr.show}` is not a top-level object; its owner `${owner.name}` is not a top-level object nor a package.")
    }
    // End of copied code

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

    val tree = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner = builderTpe.typeSymbol.maybeOwner

    if (!isClass(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClass `${builderExpr.show}` is not a class.")
    }
    if (!isConcrete(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClass `${builderTpe.typeSymbol.name}` is not a concrete class.")
    }
    val constructor = builderTpe.typeSymbol.primaryConstructor
    if (!isPublic(constructor)) {
      report.error(s"The provided SporkClass `${constructor.name}` does not have a public constructor.")
    }
    if (!isNotLocal(builderTpe.typeSymbol)) {
      report.error(s"The provided SporkClass `${builderTpe.typeSymbol.name}` is not a local class.")
    }
    if (!isNotNestedInClass(builderTpe.typeSymbol.owner)) {
      report.error(s"The provided SporkClass `${builderTpe.typeSymbol.name}` is nested in a class.")
    }
    if (!containsEmptyParamList(constructor)) {
      report.error(s"The constructor of the provided SporkClass `${constructor.name}` does not have an empty parameter list.")
    }

    '{ () }
  }
}
