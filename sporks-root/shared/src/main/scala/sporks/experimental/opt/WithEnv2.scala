package sporks.experimental.opt

import upickle.default.*

import sporks.*
import sporks.Spork.*
import sporks.PackedSpork.*


object WithEnv2 {
  // Optimizations for applying a Spork/PackedSpork with contained type T1 => R
  // or T1 ?=> R directly to a Spork/PackedSpork of type T1.
  //
  // This avoids the need to pack/unpack/unwrap the Spork/PackedSpork, which is
  // what is done when using achieving the same result using the `withEnv` or
  // `packWithEnv` methods.
  //
  // Using these methods performance benefits as they avoid the need to
  // pack/unpack/unwrap, as well as by reducing the size of the contained
  // serialized data.

  extension [T1, R](spork: Spork[T1 => R]) {
    def withEnv2(env: Spork[T1]): Spork[R] =
      SporkWithEnv(spork, env)
  }

  extension [T1, R](spork: Spork[T1 ?=> R]) {
    def withCtx2(env: Spork[T1]): Spork[R] =
      SporkWithCtx(spork, env)
  }

  extension [T1, R](packed: PackedSpork[T1 => R]) {
    def packWithEnv2(env: PackedSpork[T1]): PackedSpork[R] =
      PackedWithEnv(packed, env)
  }

  extension [T1, R](packed: PackedSpork[T1 ?=> R]) {
    def packWithCtx2(env: PackedSpork[T1]): PackedSpork[R] =
      PackedWithCtx(packed, env)
  }
}
