package sporks

import upickle.default.*

import sporks.Reflect
import sporks.Packed.*


@Reflect.EnableReflectiveInstantiation
trait SporkClassBuilder[+T](val fun: T) {
  final inline def pack(): Spork[T] =
    ${ SporkClassBuilder.packMacro('this) }
}


private object SporkClassBuilder {
  import scala.quoted.*
  def packMacro[T](expr: Expr[SporkClassBuilder[T]])(using Type[T], Quotes): Expr[Spork[T]] =
    Macros.isTopLevelClass(expr)
    '{ PackedClass($expr.getClass().getName()) }
}
