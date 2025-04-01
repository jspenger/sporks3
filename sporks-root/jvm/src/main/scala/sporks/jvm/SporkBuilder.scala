package sporks.jvm

import sporks.*
import sporks.Spork.*
import sporks.PackedSpork.*
import upickle.default.*


object SporkBuilder {

  // Note: Spork Lambdas only work for the JVM.
  // To reflectively instantiate a class or object on ScalaJS or ScalaNative,
  // the class or object must static and top-level, i.e. it should not be inside
  // a method. This is not the case with the current `Spork` lambda factory.
  // For more information, see the documentation for https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[T](inline fun: T): PackedLambda[T] =
    ${ packMacro('fun) }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithEnv[E, T] =
    ${ packMacroWithEnv('env, 'fun, 'rw) }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithCtx[E, T] =
    ${ packMacroWithCtx('env, 'fun, 'rw) }

  inline def pack[T](inline fun: T): PackedLambda[T] =
    ${ packMacro('fun) }

  inline def packWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithEnv[E, T] =
    ${ packMacroWithEnv('env, 'fun, 'rw) }

  inline def packWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): PackedWithCtx[E, T] =
    ${ packMacroWithCtx('env, 'fun, 'rw) }

  inline def build[T](inline fun: T): SporkLambda[T] =
    ${ buildMacro('fun) }

  inline def buildWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: Spork[ReadWriter[E]]): SporkWithEnv[E, T] =
    ${ buildMacroWithEnv('env, 'fun, 'rw) }

  inline def buildWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spork[ReadWriter[E]]): SporkWithCtx[E, T] =
    ${ buildMacroWithCtx('env, 'fun, 'rw) }

  import scala.quoted.*

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[SporkLambdaBuilder[T]] =
    // Warning:
    // It is critical to keep the value assignment in the following code block.
    // If it is removed, the resulting lambda will start capturing outer classes
    // when directly nested inside of methods. Moreover, this is not (easily)
    // detectable by macros, as it happens in later compilation stages. For
    // example, the macro may say that the lambda has an empty parameter list,
    // but after later compilation stages, the lambda will take the outer class
    // as a parameter, thus failing the `pack()` method which assumes that it
    // has no parameters. The current solution seems to be working.
    Macros.checkBodyExpr(bodyExpr)
    '{
      val lambda = {
        class Lambda extends SporkLambdaBuilder[T]($bodyExpr)
        (new Lambda())
      }
      lambda
    }

  private def packMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[PackedLambda[T]] =
    '{ ${ applyMacro(bodyExpr) }.pack() }

  private def packMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithEnv[E, T]] =
    '{ ${ applyMacro(bodyExpr) }.pack().packWithEnv($envExpr)(using $rwExpr) }

  private def packMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[PackedWithCtx[E, T]] =
    '{ ${ applyMacro(bodyExpr) }.pack().packWithCtx($envExpr)(using $rwExpr) }

  private def buildMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[SporkLambda[T]] =
    '{ ${ applyMacro(bodyExpr) }.build() }

  private def buildMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[Spork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[SporkWithEnv[E, T]] =
    '{ ${ applyMacro(bodyExpr) }.build().withEnv($envExpr)(using $rwExpr) }

  private def buildMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[Spork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[SporkWithCtx[E, T]] =
    '{ ${ applyMacro(bodyExpr) }.build().withCtx($envExpr)(using $rwExpr) }
}
