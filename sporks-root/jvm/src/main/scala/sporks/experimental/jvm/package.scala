package sporks.experimental

import upickle.default.*
import sporks.*
import sporks.given
import sporks.jvm.*


package object jvm {

  inline def sp[T](inline fun: T): PackedSpork[T] = {
    // FIXME/BUG:
    // It is currently not possible to use the inlined function `fun` directly
    // here. The Macros will not accept it. The current workaround has a small
    // performance impact. What we want is this instead:
    // `SporkBuilder.apply[T]{ fun }`
    // Fix the bug as it is an important pattern for use in libraries, and don't
    // change this until it is fixed. The same applies to the methods below.
    SporkBuilder.apply[Unit => T](_ => fun).withEnv(())
  }

  inline def spe[E, T](inline env: E)(inline fun: E => T)(using rw: PackedSpork[ReadWriter[E]]): PackedSpork[T] = {
    SporkBuilder.apply[Unit => E => T](_ => fun).withEnv(()).withEnv(env)
  }

  inline def spc[E, T](inline env: E)(inline fun: E ?=> T)(using rw: PackedSpork[ReadWriter[E]]): PackedSpork[T] = {
    SporkBuilder.apply[Unit => E ?=> T](_ => fun).withEnv(()).withCtx(env)
  }
}
