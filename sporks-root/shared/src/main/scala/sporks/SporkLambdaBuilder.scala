package sporks

import upickle.default.*

import sporks.Reflect
import sporks.Packed.*


@Reflect.EnableReflectiveInstantiation
private[sporks] trait SporkLambdaBuilder[+T](val fun: T) {
  final inline def pack(): Spork[T] =
    ${ SporkLambdaBuilder.packMacro('this) }
}


private object SporkLambdaBuilder {
  import scala.quoted.*
  def packMacro[T](expr: Expr[SporkLambdaBuilder[T]])(using Type[T], Quotes): Expr[Spork[T]] =
    // No checks needed, all relevant checks are done in the lambda factory `Spork.apply`.
    '{ PackedLambda($expr.getClass().getName()) }
}
