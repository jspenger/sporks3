package sporks

import upickle.default.*
import scala.quoted.*

import sporks.*


sealed trait Spork[+T] {

  def unwrap(): T = {
    this match
      case SporkObject(builder) => builder.fun
      case SporkClass(builder)  => builder.fun
      case SporkLambda(builder) => builder.fun
      case SporkEnv(env, rw)    => env
      case SporkWithEnv(spork, env) => spork.unwrap()(env.unwrap())
      case SporkWithCtx(spork, env) => spork.unwrap()(using env.unwrap())
  }

  def pack(): PackedSpork[T] = {
    this match
      case SporkObject(builder) => PackedObject(builder.getClass().getName())
      case SporkClass(builder)  => PackedClass (builder.getClass().getName())
      case SporkLambda(builder) => PackedLambda(builder.getClass().getName())
      case SporkEnv(env, rw)    => PackedEnv(write(env)(using rw.unwrap()), rw.pack())
      case SporkWithEnv(spork, env) => PackedWithEnv(spork.pack(), env.pack())
      case SporkWithCtx(spork, env) => PackedWithCtx(spork.pack(), env.pack())
  }

  def withEnv[T1, R](env: T1)(using rw: Spork[ReadWriter[T1]])(using ev: (Spork[T] <:< Spork[T1 => R])): SporkWithEnv[T1, R] = {
    SporkWithEnv(this, SporkEnv(env, rw))
  }

  def withCtx[T1, R](env: T1)(using rw: Spork[ReadWriter[T1]])(using ev: (Spork[T] <:< Spork[T1 ?=> R])): SporkWithCtx[T1, R] = {
    SporkWithCtx(this, SporkEnv(env, rw))
  }
}


// Static:
final case class SporkObject[+T](builder: SporkObjectBuilder[T]) extends Spork[T]
final case class SporkClass[+T] (builder: SporkClassBuilder[T])  extends Spork[T]
final case class SporkLambda[+T](builder: SporkLambdaBuilder[T]) extends Spork[T]
// Dynamic:
final case class SporkEnv[T]        (env: T, rw: Spork[ReadWriter[T]])     extends Spork[T]
final case class SporkWithEnv[E, +T](spork: Spork[E => T],  env: Spork[E]) extends Spork[T]
final case class SporkWithCtx[E, +T](spork: Spork[E ?=> T], env: Spork[E]) extends Spork[T]
