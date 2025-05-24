package sporks

import scala.annotation.implicitNotFound
import upickle.default.*


sealed trait Spork[+T] {
  import sporks.Packed.*

  def withEnv[T1, R](env: T1)(using prw: Spork[ReadWriter[T1]])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): Spork[R] = {
    PackedWithEnv(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `Spork[T1 => R]` directly to the
    * contents of a `Spork[T1]`.
    *
    * This avoids the need to pack and unwrap the Spork, which is what
    * otherwise is necessary for achieving the same result by using `withEnv`.
    *
    * Using this method has performance benefits as it avoids unnecessary
    * packing and unwrapping, as well as by reducing the size of the contained
    * serialized data.
    */
  def withEnv2[T1, R](env: Spork[T1])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): Spork[R] = {
    PackedWithEnv(this, env)
  }

  def withCtx[T1, R](env: T1)(using prw: Spork[ReadWriter[T1]])(using @implicitNotFound(CanWithCtx.MSG) ev: CanWithCtx[T, T1, R]): Spork[R] = {
    PackedWithCtx(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `Spork[T1 ?=> R]` directly to the
    * contents of a `Spork[T1]`.
    *
    * This avoids the need to pack and unwrap the Spork, which is what
    * otherwise is necessary for achieving the same result by using `withEnv`.
    *
    * Using this method has performance benefits as it avoids unnecessary
    * packing and unwrapping, as well as by reducing the size of the contained
    * serialized data.
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
