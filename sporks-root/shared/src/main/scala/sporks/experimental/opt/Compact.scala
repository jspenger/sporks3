package sporks.experimental.opt

import upickle.default.*

import sporks.*
import sporks.given
import sporks.Spork.*
import sporks.PackedSpork.*


object Compact {

  extension [T](spork: Spork[T]) {
    def compact(): Spork[T] = {
      spork match
        case SporkObject(_) => spork
        case SporkClass(_)  => spork
        case SporkLambda(_) => spork
        case SporkEnv(env, rw) => env match {
          case x @ SporkObject(_) => x.compact().asInstanceOf[Spork[T]]
          case x @ SporkClass(_)  => x.compact().asInstanceOf[Spork[T]]
          case x @ SporkLambda(_) => x.compact().asInstanceOf[Spork[T]]
          case x @ SporkEnv(_, _) => x.compact().asInstanceOf[Spork[T]]
          case x @ SporkWithEnv(_, _) => x.compact().asInstanceOf[Spork[T]]
          case x @ SporkWithCtx(_, _) => x.compact().asInstanceOf[Spork[T]]
          case _ => spork
        }
        case SporkWithEnv(spork, env) => SporkWithEnv(spork.compact(), env.compact())
        case SporkWithCtx(spork, env) => SporkWithCtx(spork.compact(), env.compact())
    }
  }

  extension [T](spork: Spork[T]) {
    def compact0()(using rw: Spork[ReadWriter[T]]): Spork[T] = {
      SporkEnv(spork.unwrap(), rw)
    }
  }

  extension [T](spork: PackedSpork[T]) {
    def compact(): PackedSpork[T] = {
      spork match
        case PackedObject(_) => spork
        case PackedClass(_)  => spork
        case PackedLambda(_) => spork
        case PackedEnv(env, rw) => read(env)(using rw.unwrap()) match {
          case x @ PackedObject(_) => x.compact().asInstanceOf[PackedSpork[T]]
          case x @ PackedClass(_)  => x.compact().asInstanceOf[PackedSpork[T]]
          case x @ PackedLambda(_) => x.compact().asInstanceOf[PackedSpork[T]]
          case x @ PackedEnv(_, _) => x.compact().asInstanceOf[PackedSpork[T]]
          case x @ PackedWithEnv(_, _) => x.compact().asInstanceOf[PackedSpork[T]]
          case x @ PackedWithCtx(_, _) => x.compact().asInstanceOf[PackedSpork[T]]
          case _ => spork
        }
        case PackedWithEnv(spork, env) => PackedWithEnv(spork.compact(), env.compact())
        case PackedWithCtx(spork, env) => PackedWithCtx(spork.compact(), env.compact())
    }
  }

  extension [T](spork: PackedSpork[T]) {
    def compact0()(using rw: PackedSpork[ReadWriter[T]]): PackedSpork[T] = {
      PackedEnv(write(spork.unwrap())(using rw.unwrap()), rw)
    }
  }
}
