package sporks

import upickle.default.*

import sporks.Reflect
import sporks.Packed.*


/** A builder trait that packs a [[Spork]] with a closure of type `T`. Extend
  * this trait from a **top-level object** and provide the closure as a trait
  * parameter.
  *
  * Note: Use [[SporkClassBuilder]] if type parameters are needed.
  *
  * Note: Must be extended from a top-level object. An object is considered
  * top-level if it is nested directly inside a package, or nested inside
  * another top-level object. Extending from a class will result in a
  * compile-time error when calling [[pack]].
  *
  * @example
  *   {{{
  * object MyBuilder extends SporkBuilder[Int => String](x => x.toString().reverse)
  * val mySpork: Spork[Int => String] = MyBuilder.pack()
  *   }}}
  *
  * @tparam T
  *   The type of the wrapped closure.
  * @param fun
  *   The wrapped closure.
  */
@Reflect.EnableReflectiveInstantiation
trait SporkBuilder[+T](private[sporks] val fun: T) {

  /** Packs the wrapped closure into a [[Spork]] of type `T`.
    *
    * @return
    *   A new Spork with the wrapped closure.
    */
  final inline def pack(): Spork[T] = {
    ${ SporkBuilder.packMacro('this) }
  }
}


private object SporkBuilder {
  import scala.quoted.*

  def packMacro[T](expr: Expr[SporkBuilder[T]])(using Type[T], Quotes): Expr[Spork[T]] = {
    Macros.isTopLevelObject(expr)
    '{ PackedObject($expr.getClass().getName()) }
  }
}
