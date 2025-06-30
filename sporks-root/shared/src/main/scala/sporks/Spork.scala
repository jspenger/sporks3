package sporks

import scala.annotation.implicitNotFound
import upickle.default.*


/** A serializable closure of type `T`. Guaranteed to not cause runtime errors
  * when created, serialized, deserialized, and unwrapped.
  *
  * Use [[unwrap]] to extract the packed closure.
  *
  * Use [[withEnv]] to partially apply the closure of type `T1 => R` to a value
  * of type `T1`. Use [[withEnv2]] to apply it to a value of type `Spork[T1]`.
  *
  * Use [[withCtx]] to partially apply the closure of type `T1 ?=> R` to a value
  * of type `T1`. Use [[withCtx2]] to apply it to a value of type `Spork[T1]`.
  *
  * Sporks are created by:
  *   - (JVM) The sporks.jvm.Spork lambda factories: `sp`, `spe`, `spc`.
  *     Requires explicit capture of environment variables.
  *   - (JVM) The sporks.jvm.AutoCapture lambda factory: `spauto`. Implicitly
  *     captures environment variables.
  *   - (JVM, Native, ScalaJS) Packing a top-level object which extends the
  *     [[SporkBuilder]] trait.
  *   - (JVM, Native, ScalaJS) Packing a top-level class which extends the
  *     [[SporkClassBuilder]] trait.
  *   - (JVM, Native, ScalaJS) The [[Env]] factory for packing a value of type
  *     `T` for which there is a Spork[ReadWriter[T]].
  *
  * Serializing and deserializing a Spork is easiest done by using the `upickle`
  * library.
  *
  * Compile-time macros guarantee that it is safe to create, serialize,
  * deserialize, and unwrap the packed closure. Creating a Spork is gauranteed
  * to not cause runtime errors. Serializing, deserializing, and unwrapping a
  * Spork is guaranteed to not cause runtime errors.
  *
  * @example
  *   {{{
  * val mySpork: Spork[Int => String] = sp { x => x.toString.reverse }
  * val myAppliedSpork: Spork[String] = mySpork.withEnv(10)
  * val serialized = upickle.default.write(myAppliedSpork)
  * val deserialized = upickle.default.read[Spork[String]](serialized)
  * val unwrapped = deserialized.unwrap()
  * unwrapped // "01"
  *   }}}
  *
  * @tparam T
  *   The type of the packed closure.
  */
sealed trait Spork[+T] {
  import sporks.Packed.*

  /** Applies the packed closure to a value of type `T1`. Only available if the
    * wrapped closure of type `T` is a subtype of `T1 => R`.
    *
    * The value is packed together with the packed closure. When unwrapping,
    * both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The value applied to the packed closure.
    * @param prw
    *   The implicit `Spork[ReadWriter[T1]]` used for packing the `env`.
    * @tparam T1
    *   The type of the value applied to the packed closure.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spork[R]` with the result of the application.
    */
  def withEnv[T1, R](env: T1)(using prw: Spork[ReadWriter[T1]])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): Spork[R] = {
    PackedWithEnv(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `Spork[T1 => R]` directly to a `Spork[T1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spork.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `T1 =>
    * R`.
    */
  def withEnv2[T1, R](env: Spork[T1])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): Spork[R] = {
    PackedWithEnv(this, env)
  }

  /** Applies the packed closure to a context value of type `T1`. Only available
    * if the wrapped closure of type `T` is a subtype of `T1 ?=> R`.
    *
    * The context value is packed together with the packed closure. When
    * unwrapping, both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The context value applied to the packed closure.
    * @param prw
    *   The implicit `Spork[ReadWriter[T1]]` used for packing the `env`.
    * @tparam T1
    *   The type of the context value applied to the packed closure.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spork[R]` with the result of the application.
    */
  def withCtx[T1, R](env: T1)(using prw: Spork[ReadWriter[T1]])(using @implicitNotFound(CanWithCtx.MSG) ev: CanWithCtx[T, T1, R]): Spork[R] = {
    PackedWithCtx(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `Spork[T1 ?=> R]` directly to a
    * `Spork[T1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spork.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `T1 ?=>
    * R`.
    */
  def withCtx2[T1, R](env: Spork[T1])(using @implicitNotFound(CanWithCtx.MSG) ev: CanWithCtx[T, T1, R]): Spork[R] = {
    PackedWithCtx(this, env)
  }

  def map[U](fun: Spork[T => U]): Spork[U] = {
    fun.withEnv2(this)
  }

  def flatMap[U](fun: Spork[T => Spork[U]]): Spork[U] = {
    fun.withEnv2(this).unwrap()
  }

  /** Unwraps and returns the packed closure.
    *
    * @return
    *   The unwrapped closure of type `T`.
    */
  def unwrap(): T = {
    this match
      case PackedObject(fun) => Reflect.getModuleFieldValue[SporkBuilder[T]](fun).fun
      case PackedClass(fun)  => Reflect.getClassInstance[SporkClassBuilder[T]](fun).fun
      case PackedLambda(fun) => Reflect.getClassInstance[SporkLambdaBuilder[T]](fun).fun
      case PackedEnv(env, rw) => read(env)(using rw.unwrap())
      case PackedWithEnv(packed, packedEnv) => packed.unwrap()(packedEnv.unwrap())
      case PackedWithCtx(packed, packedEnv) => packed.unwrap()(using packedEnv.unwrap())
  }
}


private object Packed {

  // Static:
  final case class PackedObject[+T](fun: String) extends Spork[T]
  final case class PackedClass[+T] (fun: String) extends Spork[T]
  final case class PackedLambda[+T](fun: String) extends Spork[T]
  // Dynamic:
  final case class PackedEnv[E]        (env: String, rw: Spork[ReadWriter[E]])       extends Spork[E]
  final case class PackedWithEnv[E, +T](packed: Spork[E => T],  packedEnv: Spork[E]) extends Spork[T]
  final case class PackedWithCtx[E, +T](packed: Spork[E ?=> T], packedEnv: Spork[E]) extends Spork[T]

}


private type CanWithEnv[T, T1, R] = Spork[T] <:< Spork[T1 => R]
private object CanWithEnv { inline val MSG = "Cannot pack contained type ${T} with environment type ${T1}. It is not a function type of ${T1} => ${R}." }

private type CanWithCtx[T, T1, R] = Spork[T] <:< Spork[T1 ?=> R]
private object CanWithCtx { inline val MSG = "Cannot pack contained type ${T} with context type ${T1}. It is not a function type of ${T1} ?=> ${R}." }
