package sporks.jvm

import upickle.default.*

import sporks.*
import sporks.Packed.*


object Spork {

  // Note: Spork Lambdas only work for the JVM.
  // To reflectively instantiate a class or object on ScalaJS or ScalaNative,
  // the class or object must static and top-level, i.e. it should not be inside
  // a method. This is not the case with the current `Spork` lambda factory.
  // For more information, see the documentation for https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[T](inline fun: T): Spork[T] =
    ${ packMacro('fun) }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: Spork[ReadWriter[E]]): Spork[T] =
    ${ packMacroWithEnv('env, 'fun, 'rw) }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spork[ReadWriter[E]]): Spork[T] =
    ${ packMacroWithCtx('env, 'fun, 'rw) }

  import scala.quoted.*

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[SporkLambdaBuilder[T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporkLambdaBuilder[T]($bodyExpr)
      (new Lambda())
    }

  private def packMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[Spork[T]] =
    '{ ${ applyMacro(bodyExpr) }.pack() }

  private def packMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[Spork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[Spork[T]] =
    '{ ${ applyMacro(bodyExpr) }.pack().withEnv($envExpr)(using $rwExpr) }

  private def packMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[Spork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[Spork[T]] =
    '{ ${ applyMacro(bodyExpr) }.pack().withCtx($envExpr)(using $rwExpr) }

}
