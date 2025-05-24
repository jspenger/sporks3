package sporks

import upickle.default.*

import sporks.*
import sporks.Packed.*


object Env {
  def apply[T](env: T)(using rw: Spork[ReadWriter[T]]): Spork[T] =
    PackedEnv(write(env)(using rw.unwrap()), rw)
}
