package sporks

import scala.annotation.implicitNotFound
import upickle.default.*


sealed trait PackedSpork[+T] {
  import sporks.Spork.*
  import sporks.PackedSpork.*

  def withEnv[T1, R](env: T1)(using prw: PackedSpork[ReadWriter[T1]])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): PackedWithEnv[T1, R] = {
    PackedWithEnv(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `PackedSpork[T1 => R]` directly to the
    * contents of a `PackedSpork[T1]`.
    *
    * This avoids the need to pack and unwrap the PackedSpork, which is what
    * otherwise is necessary for achieving the same result by using `withEnv`.
    *
    * Using this method has performance benefits as it avoids unnecessary
    * packing and unwrapping, as well as by reducing the size of the contained
    * serialized data.
    */
  def withEnv2[T1, R](env: PackedSpork[T1])(using @implicitNotFound(CanWithEnv.MSG) ev: CanWithEnv[T, T1, R]): PackedWithEnv[T1, R] = {
    PackedWithEnv(this, env)
  }

  def withCtx[T1, R](env: T1)(using prw: PackedSpork[ReadWriter[T1]])(using @implicitNotFound(CanWithCtx.MSG) ev: CanWithCtx[T, T1, R]): PackedWithCtx[T1, R] = {
    PackedWithCtx(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  /** Optimization for applying this `PackedSpork[T1 ?=> R]` directly to the
    * contents of a `PackedSpork[T1]`.
    *
    * This avoids the need to pack and unwrap the PackedSpork, which is what
    * otherwise is necessary for achieving the same result by using `withEnv`.
    *
    * Using this method has performance benefits as it avoids unnecessary
    * packing and unwrapping, as well as by reducing the size of the contained
    * serialized data.
    */
  def withCtx2[T1, R](env: PackedSpork[T1])(using @implicitNotFound(CanWithCtx.MSG) ev: CanWithCtx[T, T1, R]): PackedWithCtx[T1, R] = {
    PackedWithCtx(this, env)
  }

  def map[U](fun: T => U)(using rw: PackedSpork[ReadWriter[U]]): PackedSpork[U] = {
    PackedEnv(write(fun.apply(this.unwrap()))(using rw.unwrap()), rw)
  }

  def map2[U](fun: PackedSpork[T => U]): PackedSpork[U] = {
    fun.withEnv2(this)
  }

  def flatMap[U](fun: T => PackedSpork[U])(using rw: PackedSpork[ReadWriter[U]]): PackedSpork[U] = {
    PackedEnv(write(fun.apply(this.unwrap()).unwrap())(using rw.unwrap()), rw)
  }

  def flatMap2[U](fun: PackedSpork[T => PackedSpork[U]]): PackedSpork[U] = {
    fun.withEnv2(this).unwrap()
  }

  def unwrap(): T = {
    this match
      case PackedObject(fun) => SporkObjectBuilder.fromString[T](fun).fun
      case PackedClass(fun)  => SporkClassBuilder.fromString[T](fun).fun
      case PackedLambda(fun) => SporkLambdaBuilder.fromString[T](fun).fun
      case PackedEnv(env, rw) => read(env)(using rw.unwrap())
      case PackedWithEnv(packed, packedEnv) => packed.unwrap()(packedEnv.unwrap())
      case PackedWithCtx(packed, packedEnv) => packed.unwrap()(using packedEnv.unwrap())
  }

  def unpack(): Spork[T] = {
    this match
      case PackedObject(fun) => SporkObject(SporkObjectBuilder.fromString(fun))
      case PackedClass(fun)  => SporkClass(SporkClassBuilder.fromString(fun))
      case PackedLambda(fun) => SporkLambda(SporkLambdaBuilder.fromString(fun))
      case PackedEnv(env, rw)               => SporkEnv(read(env)(using rw.unpack().unwrap()), rw.unpack())
      case PackedWithEnv(packed, packedEnv) => SporkWithEnv(packed.unpack(), packedEnv.unpack())
      case PackedWithCtx(packed, packedEnv) => SporkWithCtx(packed.unpack(), packedEnv.unpack())
  }
}


object PackedSpork {

  // Static:
  final case class PackedObject[+T](fun: String) extends PackedSpork[T]
  final case class PackedClass[+T] (fun: String) extends PackedSpork[T]
  final case class PackedLambda[+T](fun: String) extends PackedSpork[T]
  // Dynamic:
  final case class PackedEnv[E]        (env: String, rw: PackedSpork[ReadWriter[E]])             extends PackedSpork[E]
  final case class PackedWithEnv[E, +T](packed: PackedSpork[E => T],  packedEnv: PackedSpork[E]) extends PackedSpork[T]
  final case class PackedWithCtx[E, +T](packed: PackedSpork[E ?=> T], packedEnv: PackedSpork[E]) extends PackedSpork[T]

  private type CanWithEnv[T, T1, R] = PackedSpork[T] <:< PackedSpork[T1 => R]
  private object CanWithEnv { inline val MSG = "Cannot pack contained type ${T} with environment type ${T1}. It is not a function type of ${T1} => ${R}." }

  private type CanWithCtx[T, T1, R] = PackedSpork[T] <:< PackedSpork[T1 ?=> R]
  private object CanWithCtx { inline val MSG = "Cannot pack contained type ${T} with context type ${T1}. It is not a function type of ${T1} ?=> ${R}." }
}
