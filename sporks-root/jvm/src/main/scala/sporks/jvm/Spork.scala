package sporks.jvm

import upickle.default.*

import sporks.*
import sporks.Packed.*


/** A factory for creating Sporks that are safe to serialize and deserialize.
  *
  * Note: If a variable is captured then the code will not compile. Use the
  * [[sporks.jvm.AutoCapture]] factory if you want to implicitly capture
  * variables. See the docs for a full discussion on when variables are
  * captured.
  *
  * Note: The Spork factory only works on the JVM. Use the
  * [[sporks.SporkBuilder]] or [[sporks.SporkClassBuilder]] if ScalaJS or
  * ScalaNative support is needed.
  */
object Spork {

  // The Spork factory only works on the JVM. The generated class here is not a
  // top-level class. For this reason, it cannot be reflectively instantiated on
  // ScalaJS or ScalaNative. For more information, see:
  // https://github.com/portable-scala/portable-scala-reflect.

  /** Create a Spork from the provided closure `fun`.
    *
    * The created Spork is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compiler error.
    *
    * To capture variables, use [[applyWithEnv]], [[applyWithCtx]], or
    * [[sporks.jvm.AutoCapture]] instead.
    *
    * @example
    *   {{{
    * val mySpork = Spork.apply[Int => String] { x => x.toString.reverse }
    *   }}}
    *
    * @param fun
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spork[T]` with the packed closure `fun`.
    */
  inline def apply[T](inline fun: T): Spork[T] = {
    ${ applyMacro('fun) }
  }

  /** Create a Spork from the provided closure `fun` with an environment
    * variable `env` as the first parameter of the closure.
    *
    * The created Spork is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compiler error.
    *
    * @example
    *   {{{
    * val mySpork = Spork.applyWithEnv[Int, String](11) { env => (env + 12).toString.reverse }
    *   }}}
    *
    * @param env
    *   The environment variable applied to the closure.
    * @param fun
    *   The closure.
    * @param rw
    *   The implicit `Spork[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spork[T]` with the packed closure `fun` applied to the `env`.
    */
  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
    apply(fun).withEnv(env)(using rw)
  }

  /** Create a Spork from the provided closure `fun` with an environment
    * variable `env` as the first implicit parameter of the closure.
    *
    * The created Spork is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compiler error.
    *
    * @example
    *   {{{
    * val mySpork = Spork.applyWithCtx[Int, String](11) { env ?=> (env + 12).toString.reverse }
    *   }}}
    *
    * @param env
    *   The context environment variable applied to the closure.
    * @param fun
    *   The closure.
    * @param rw
    *   The implicit `Spork[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the context environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spork[T]` with the packed closure `fun` using the implicit `env`.
    */
  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
    apply[E ?=>T](fun).withCtx(env)(using rw)
  }

  import scala.quoted.*

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[Spork[T]] = {
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporkLambdaBuilder[T]($bodyExpr)
      (new Lambda()).pack()
    }
  }
}
