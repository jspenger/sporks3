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
    // Note:
    // It is critical to keep the value assignment in the following code block.
    // If it is removed, the resulting lambda will start capturing outer classes
    // when directly nested inside of methods. Moreover, this is not (easily)
    // detectable by macros, as it happens in later compilation stages. For
    // example, the macro may say that the lambda has an empty parameter list,
    // but after later compilation stages, the lambda will take the outer class
    // as a parameter, thus failing the `build()` method which assumes that it
    // has no parameters. The current solution seems to be working.

    Macros.checkBodyExpr(bodyExpr)
    '{
       val lambda = {
         class Lambda extends SporkLambda[T]($bodyExpr)
         (new Lambda()).pack()
       }
       lambda
    }

  private def applyMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithEnv[E, T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      val lambda = {
        class Lambda extends SporkLambda[E => T]($bodyExpr)
        (new Lambda()).pack().packWithEnv($envExpr)(using $rwExpr)
      }
      lambda
    }

  private def applyMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithCtx[E, T]] =
    Macros.checkBodyExpr(bodyExpr)
    '{
      val lambda = {
        class Lambda extends SporkLambda[E ?=> T]($bodyExpr.apply)
        (new Lambda()).pack().packWithCtx($envExpr)(using $rwExpr)
      }
      lambda
    }
}
