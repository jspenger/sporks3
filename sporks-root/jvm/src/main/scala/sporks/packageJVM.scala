package sporks

import upickle.default.*

import sporks.*
import sporks.given
import sporks.jvm.*


inline def sp[T](inline fun: T): Spork[T] = {
  Spork.apply[T](fun)
}

inline def spe[E, T](inline env: E)(inline fun: E => T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
  Spork.apply[E => T](fun).withEnv(env)
}

inline def spc[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
  Spork.apply[E ?=> T](fun).withCtx(env)
}

inline def spauto[T](inline fun: T): Spork[T] = {
  AutoCapture.apply[T](fun)
}

inline def spenv[T](inline env: T)(using rw: Spork[ReadWriter[T]]): Spork[T] = {
  Env.apply[T](env)
}
