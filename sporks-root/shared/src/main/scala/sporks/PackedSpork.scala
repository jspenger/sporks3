package sporks

import scala.annotation.implicitNotFound
import upickle.default.*


sealed trait PackedSpork[+T] {
  import sporks.Spork.*
  import sporks.PackedSpork.*

  def packWithEnv[T1, R](env: T1)(using prw: PackedSpork[ReadWriter[T1]])(using @implicitNotFound(CanPackWithEnv.MSG) ev: CanPackWithEnv[T, T1, R]): PackedWithEnv[T1, R] = {
    PackedWithEnv(this, PackedEnv(write(env)(using prw.unwrap()), prw))
  }

  def packWithCtx[T1, R](env: T1)(using prw: PackedSpork[ReadWriter[T1]])(using @implicitNotFound(CanPackWithCtx.MSG) ev: CanPackWithCtx[T, T1, R]): PackedWithCtx[T1, R] = {
    PackedWithCtx(this, PackedEnv(write(env)(using prw.unwrap()), prw))
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

  private type CanPackWithEnv[T, T1, R] = PackedSpork[T] <:< PackedSpork[T1 => R]
  private object CanPackWithEnv { inline val MSG = "Cannot pack contained type ${T} with environment type ${T1}. It is not a function type of ${T1} => ${R}." }

  private type CanPackWithCtx[T, T1, R] = PackedSpork[T] <:< PackedSpork[T1 ?=> R]
  private object CanPackWithCtx { inline val MSG = "Cannot pack contained type ${T} with context type ${T1}. It is not a function type of ${T1} ?=> ${R}." }
}
