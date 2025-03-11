package sporks.experimental.jvm

import upickle.default.*

import sporks.*
import sporks.jvm.*
import sporks.experimental.*

object SporkFun {
  inline def apply[T](inline fun: T): SporkFun[T] = {
    ${ applyMacro('fun) }
  }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): SporkFun[T] = {
    ${ applyMacroWithEnv('env, 'fun, 'rw) }
  }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): SporkFun[T] = {
    ${ applyMacroWithCtx('env, 'fun, 'rw) }
  }

  import scala.quoted.*
  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[SporkFun[T]] = {
    val packedLambdaExpr = Spork.applyMacro(bodyExpr)
    '{ new SporkFunImpl[T]($bodyExpr, $packedLambdaExpr) }
  }

  private[sporks] def applyMacroWithEnv[E, T](envExpr: Expr[E], bodyExpr: Expr[E => T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[SporkFun[T]] = {
    val packedLambdaExpr = Spork.applyMacro(bodyExpr)
    '{ new SporkFunImpl[E => T]($bodyExpr, $packedLambdaExpr).sporkfun_withEnv($envExpr)(using $rwExpr) }
  }

  private[sporks] def applyMacroWithCtx[E, T](envExpr: Expr[E], bodyExpr: Expr[E ?=> T], rwExpr: Expr[PackedSpork[ReadWriter[E]]])(using Type[E], Type[T], Quotes): Expr[SporkFun[T]] = {
    val packedLambdaExpr = Spork.applyMacro(bodyExpr)
    '{ new SporkFunImpl[E ?=> T]($bodyExpr.apply, $packedLambdaExpr).sporkfun_withCtx($envExpr)(using $rwExpr) }
  }
}
