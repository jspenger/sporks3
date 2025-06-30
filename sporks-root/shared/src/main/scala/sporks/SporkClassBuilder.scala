package sporks

import upickle.default.*

import sporks.Reflect
import sporks.Packed.*


/** A class-based builder trait that packs a [[Spork]] with a closure of type
  * `T`. Extend this trait from a **top-level class** and provide the closure as
  * a trait parameter.
  *
  * Note: To be used instead of [[SporkBuilder]] when type parameters are
  * needed.
  *
  * Note: Must be extended from a top-level class. A class is considered
  * top-level if it is nested directly inside a package, or nested inside a
  * top-level object. Extending from an object will result in a compile-time
  * error when calling [[pack]].
  *
  * @example
  *   {{{
  * class MyBuilder[T] extends SporkClassBuilder[T => String](x => x.toString().reverse)
  * val mySpork: Spork[Int => String] = new MyBuilder[Int]().pack()
  *   }}}
  *
  * @tparam T
  *   The type of the wrapped closure.
  * @param fun
  *   The wrapped closure.
  */
@Reflect.EnableReflectiveInstantiation
trait SporkClassBuilder[+T](private[sporks] val fun: T) {

  /** Packs the wrapped closure into a [[Spork]] of type `T`.
    *
    * @return
    *   A new Spork with the wrapped closure.
    */
  final inline def pack(): Spork[T] = {
    ${ SporkClassBuilder.packMacro('this) }
  }
}


private object SporkClassBuilder {
  import scala.quoted.*

  def packMacro[T](expr: Expr[SporkClassBuilder[T]])(using Type[T], Quotes): Expr[Spork[T]] = {
    Macros.isTopLevelClass(expr)
    '{ PackedClass($expr.getClass().getName()) }
  }
}
