package sporks

import upickle.default.*

import sporks.*
import sporks.Packed.*


/** A factory for packing environment values of type `T` into `Spork[T]` by
  * using an implicit `Spork[ReadWriter[T]]` instance.
  */
object Env {

  /** Packs a value of type `T` as a `Spork[T]`.
    *
    * @param env
    *   The value to pack.
    * @param rw
    *   The implicit `Spork[ReadWriter[T]]` used for packing the `env`.
    * @tparam T
    *   The type of the value to pack.
    * @return
    *   A new `Spork[T]` with the packed `env`.
    */
  def apply[T](env: T)(using rw: Spork[ReadWriter[T]]): Spork[T] = {
    PackedEnv(write(env)(using rw.unwrap()), rw)
  }
}
