package sporks.experimental

import upickle.default.*
import sporks.*
import sporks.given
import sporks.jvm.*


package object jvm {

  inline def sp[T](inline fun: T): PackedSpork[T] = {
    SporkBuilder.apply[T](fun)
  }

  inline def spe[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): PackedSpork[T] = {
    SporkBuilder.apply[E => T](fun).withEnv(env)
  }

  inline def spc[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): PackedSpork[T] = {
    SporkBuilder.apply[E ?=> T](fun).withCtx(env)
  }

  inline def spx[T](inline fun: T): PackedSpork[T] = {
    Lift.apply[T](fun)
  }
}
