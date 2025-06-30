package sporks

import upickle.default.*

import sporks.*
import sporks.jvm.*


/** Alias for [[sporks.jvm.Spork.apply]]. */
inline def sp[T](inline fun: T): Spork[T] = {
  Spork.apply[T](fun)
}

/** Alias for [[sporks.jvm.Spork.applyWithEnv]]. */
inline def spe[E, T](inline env: E)(inline fun: E => T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
  Spork.apply[E => T](fun).withEnv(env)
}

/** Alias for [[sporks.jvm.Spork.applyWithCtx]]. */
inline def spc[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spork[ReadWriter[E]]): Spork[T] = {
  Spork.apply[E ?=> T](fun).withCtx(env)
}

/** Alias for [[sporks.jvm.AutoCapture.apply]]. */
inline def spauto[T](inline fun: T): Spork[T] = {
  AutoCapture.apply[T](fun)
}

/** Alias for [[sporks.Env.apply]]. */
inline def spenv[T](inline env: T)(using rw: Spork[ReadWriter[T]]): Spork[T] = {
  Env.apply[T](env)
}
