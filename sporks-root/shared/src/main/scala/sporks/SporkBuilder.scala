package sporks

import upickle.default.*

import sporks.Reflect
import sporks.Packed.*


@Reflect.EnableReflectiveInstantiation
trait SporkBuilder[+T](val fun: T) {
  final inline def pack(): Spork[T] =
    ${ SporkBuilder.packMacro('this) }
}


private object SporkBuilder {
  import scala.quoted.*
  def packMacro[T](expr: Expr[SporkBuilder[T]])(using Type[T], Quotes): Expr[Spork[T]] =
    Macros.isTopLevelObject(expr)
    '{ PackedObject($expr.getClass().getName()) }
}
