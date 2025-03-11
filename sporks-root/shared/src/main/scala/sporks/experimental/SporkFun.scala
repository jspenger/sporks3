package sporks.experimental

import sporks.*
import upickle.default.*

////////////////////////////////////////////////////////////////////////////////
// SporkFun
////////////////////////////////////////////////////////////////////////////////

sealed trait SporkFun[+T] {
  private[sporks] def fun: T
  private[sporks] def packed: PackedSpork[T]
}

private[sporks] class SporkFunImpl[+T](
    private[sporks] override val fun: T,
    private[sporks] override val packed: PackedSpork[T]
) extends SporkFun[T]

////////////////////////////////////////////////////////////////////////////////
// sporkfun_build()
////////////////////////////////////////////////////////////////////////////////

extension [T](inline builder1: SporkObject[T]) {
  inline def sporkfun_build(): SporkFun[T] = {
    new SporkFunImpl[T](builder1.fun, builder1.pack())
  }
}

extension [T](inline builder1: SporkClass[T]) {
  inline def sporkfun_build(): SporkFun[T] = {
    new SporkFunImpl[T](builder1.fun, builder1.pack())
  }
}

////////////////////////////////////////////////////////////////////////////////
// sporkfun_unwrap()
////////////////////////////////////////////////////////////////////////////////

extension [T](fun1: SporkFun[T]) {
  def sporkfun_unwrap(): T = {
    fun1.fun
  }
}

////////////////////////////////////////////////////////////////////////////////
// sporkfun_withEnv() / sporkfun_withCtx()
////////////////////////////////////////////////////////////////////////////////

extension [T, R](fun1: SporkFun[Function1[T, R]]) {
  def sporkfun_withEnv(env1: T)(using prw: PackedSpork[ReadWriter[T]]): SporkFun[R] = {
    new SporkFunImpl[R](fun1.fun(env1), fun1.packed.packWithEnv(env1)(using prw))
  }
}

extension [T, R](fun1: SporkFun[ContextFunction1[T, R]]) {
  def sporkfun_withCtx(ctx1: T)(using prw: PackedSpork[ReadWriter[T]]): SporkFun[R] = {
    new SporkFunImpl[R](fun1.fun.apply(using ctx1), fun1.packed.packWithCtx(ctx1)(using prw))
  }
}

////////////////////////////////////////////////////////////////////////////////
// sporkfun_pack() / sporkfun_unpack()
////////////////////////////////////////////////////////////////////////////////

extension [T1](fun1: SporkFun[T1]) {
  def sporkfun_pack(): PackedSpork[T1] = {
    fun1.packed
  }
}

extension [T1](packed1: PackedSpork[T1]) {
  def sporkfun_unpack(): SporkFun[T1] = {
    new SporkFunImpl[T1](packed1.build(), packed1)
  }
}

////////////////////////////////////////////////////////////////////////////////
// sporkfun_tupledN() / sporkfun_untupledN()
////////////////////////////////////////////////////////////////////////////////

extension [R](fun1: SporkFun[Function0[R]]) {
  def sporkfun_tupled0(): SporkFun[Function1[EmptyTuple, R]] = {
    fun1.packed.tupled0().sporkfun_unpack()
  }
}

extension [T1, R](fun1: SporkFun[Function1[T1, R]]) {
  def sporkfun_tupled1(): SporkFun[Function1[Tuple1[T1], R]] = {
    fun1.packed.tupled1().sporkfun_unpack()
  }
}
extension [T1, T2, R](fun1: SporkFun[Function2[T1, T2, R]]) {
  def sporkfun_tupled2(): SporkFun[Function1[Tuple2[T1, T2], R]] = {
    fun1.packed.tupled2().sporkfun_unpack()
  }
}
extension [T1, T2, T3, R](fun1: SporkFun[Function3[T1, T2, T3, R]]) {
  def sporkfun_tupled3(): SporkFun[Function1[Tuple3[T1, T2, T3], R]] = {
    fun1.packed.tupled3().sporkfun_unpack()
  }
}
extension [R](fun1: SporkFun[Function1[EmptyTuple, R]]) {
  def sporkfun_untupled0(): SporkFun[Function0[R]] = {
    fun1.packed.untupled0().sporkfun_unpack()
  }
}
extension [T1, R](fun1: SporkFun[Function1[Tuple1[T1], R]]) {
  def sporkfun_untupled1(): SporkFun[Function1[T1, R]] = {
    fun1.packed.untupled1().sporkfun_unpack()
  }
}
extension [T1, T2, R](fun1: SporkFun[Function1[Tuple2[T1, T2], R]]) {
  def sporkfun_untupled2(): SporkFun[Function2[T1, T2, R]] = {
    fun1.packed.untupled2().sporkfun_unpack()
  }
}
extension [T1, T2, T3, R](fun1: SporkFun[Function1[Tuple3[T1, T2, T3], R]]) {
  def sporkfun_untupled3(): SporkFun[Function3[T1, T2, T3, R]] = {
    fun1.packed.untupled3().sporkfun_unpack()
  }
}

////////////////////////////////////////////////////////////////////////////////
// curriedN() / uncurriedN()
////////////////////////////////////////////////////////////////////////////////

extension [R](fun1: SporkFun[Function0[R]]) {
  def sporkfun_curried0(): SporkFun[Function0[R]] = {
    fun1.packed.curried0().sporkfun_unpack()
  }
}

extension [T1, R](fun1: SporkFun[Function1[T1, R]]) {
  def sporkfun_curried1(): SporkFun[Function1[T1, R]] = {
    fun1.packed.curried1().sporkfun_unpack()
  }
}

extension [T1, T2, R](fun1: SporkFun[Function2[T1, T2, R]]) {
  def sporkfun_curried2(): SporkFun[Function1[T1, Function1[T2, R]]] = {
    fun1.packed.curried2().sporkfun_unpack()
  }
}

extension [T1, T2, T3, R](fun1: SporkFun[Function3[T1, T2, T3, R]]) {
  def sporkfun_curried3(): SporkFun[Function1[T1, Function1[T2, Function1[T3, R]]]] = {
    fun1.packed.curried3().sporkfun_unpack()
  }
}

extension [R](fun1: SporkFun[Function0[R]]) {
  def sporkfun_uncurried0(): SporkFun[Function0[R]] = {
    fun1.packed.uncurried0().sporkfun_unpack()
  }
}

extension [T1, R](fun1: SporkFun[Function1[T1, R]]) {
  def sporkfun_uncurried1(): SporkFun[Function1[T1, R]] = {
    fun1.packed.uncurried1().sporkfun_unpack()
  }
}

extension [T1, T2, R](fun1: SporkFun[Function1[T1, Function1[T2, R]]]) {
  def sporkfun_uncurried2(): SporkFun[Function2[T1, T2, R]] = {
    fun1.packed.uncurried2().sporkfun_unpack()
  }
}

extension [T1, T2, T3, R](fun1: SporkFun[Function1[T1, Function1[T2, Function1[T3, R]]]]) {
  def sporkfun_uncurried3(): SporkFun[Function3[T1, T2, T3, R]] = {
    fun1.packed.uncurried3().sporkfun_unpack()
  }
}
