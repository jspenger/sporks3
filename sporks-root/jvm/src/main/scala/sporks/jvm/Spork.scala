package sporks.jvm

import sporks.*
import upickle.default.*

object Spork {

  // Note: Spork Lambdas only work for the JVM.
  // To reflectively instantiate a class or object on ScalaJS or ScalaNative,
  // the class or object must static and top-level, i.e. it should not be inside
  // a method. This is not the case with the current `Spork` lambda factory.
  // For more information, see the documentation for https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[T](inline fun: T): PackedLambda[T] =
    ${ applyMacro('fun) }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithEnv[E, T] =
    ${ applyMacroWithEnv('env, 'fun, 'rw) }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithCtx[E, T] =
    ${ applyMacroWithCtx('env, 'fun, 'rw) }

  import scala.quoted.*

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[PackedLambda[T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporkLambda[T]($bodyExpr)
      (new Lambda()).pack()
    }

  private def applyMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithEnv[E, T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporkLambda[E => T]($bodyExpr)
      (new Lambda()).pack().packWithEnv($envExpr)(using $rwExpr)
    }

  private def applyMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithCtx[E, T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporkLambda[E ?=> T]($bodyExpr.apply)
      (new Lambda()).pack().packWithCtx($envExpr)(using $rwExpr)
    }
}
