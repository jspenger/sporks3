package sporks.experimental.extensions

import sporks.*
import sporks.given


object Tupled {

  private def tupled0[R](fun1: Function0[R]): Function1[EmptyTuple, R] = { case EmptyTuple => fun1() }
  private def tupled1[T1, R](fun1: Function1[T1, R]): Function1[Tuple1[T1], R] = { case Tuple1(x1) => fun1(x1) }
  private def tupled2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[(T1, T2), R] = { case Tuple2(x1, x2) => fun1(x1, x2) }
  private def tupled3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[(T1, T2, T3), R] = { case Tuple3(x1, x2, x3) => fun1(x1, x2, x3) }
  private def untupled0[R](fun1: Function1[EmptyTuple, R]): Function0[R] = { () => fun1(EmptyTuple) }
  private def untupled1[T1, R](fun1: Function1[Tuple1[T1], R]): Function1[T1, R] = { (x1) => fun1(Tuple1(x1)) }
  private def untupled2[T1, T2, R](fun1: Function1[(T1, T2), R]): Function2[T1, T2, R] = { (x1, x2) => fun1(Tuple2(x1, x2)) }
  private def untupled3[T1, T2, T3, R](fun1: Function1[(T1, T2, T3), R]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(Tuple3(x1, x2, x3)) }

  private[sporks] class Tupled0[R] extends SporkClassBuilder[PackedSpork[Function0[R]] => Function1[EmptyTuple, R]]({ fun1 => tupled0(fun1.unwrap()) })
  private[sporks] class Tupled1[T1, R] extends SporkClassBuilder[PackedSpork[Function1[T1, R]] => Function1[Tuple1[T1], R]]({ fun1 => tupled1(fun1.unwrap()) })
  private[sporks] class Tupled2[T1, T2, R] extends SporkClassBuilder[PackedSpork[Function2[T1, T2, R]] => Function1[Tuple2[T1, T2], R]]({ fun1 => tupled2(fun1.unwrap()) })
  private[sporks] class Tupled3[T1, T2, T3, R] extends SporkClassBuilder[PackedSpork[Function3[T1, T2, T3, R]] => Function1[Tuple3[T1, T2, T3], R]]({ fun1 => tupled3(fun1.unwrap()) })
  private[sporks] class Untupled0[R] extends SporkClassBuilder[PackedSpork[Function1[EmptyTuple, R]] => Function0[R]]({ fun1 => untupled0(fun1.unwrap()) })
  private[sporks] class Untupled1[T1, R] extends SporkClassBuilder[PackedSpork[Function1[Tuple1[T1], R]] => Function1[T1, R]]({ fun1 => untupled1(fun1.unwrap()) })
  private[sporks] class Untupled2[T1, T2, R] extends SporkClassBuilder[PackedSpork[Function1[(T1, T2), R]] => Function2[T1, T2, R]]({ fun1 => untupled2(fun1.unwrap()) })
  private[sporks] class Untupled3[T1, T2, T3, R] extends SporkClassBuilder[PackedSpork[Function1[(T1, T2, T3), R]] => Function3[T1, T2, T3, R]]({ fun1 => untupled3(fun1.unwrap()) })

  extension [R](packed1: PackedSpork[Function0[R]]) { def tupled0(): PackedSpork[Function1[EmptyTuple, R]] = (new Tupled0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def tupled1(): PackedSpork[Function1[Tuple1[T1], R]] = (new Tupled1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function2[T1, T2, R]]) { def tupled2(): PackedSpork[Function1[Tuple2[T1, T2], R]] = (new Tupled2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function3[T1, T2, T3, R]]) { def tupled3(): PackedSpork[Function1[Tuple3[T1, T2, T3], R]] = (new Tupled3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }
  extension [R](packed1: PackedSpork[Function1[EmptyTuple, R]]) { def untupled0(): PackedSpork[Function0[R]] = (new Untupled0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[Tuple1[T1], R]]) { def untupled1(): PackedSpork[Function1[T1, R]] = (new Untupled1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function1[Tuple2[T1, T2], R]]) { def untupled2(): PackedSpork[Function2[T1, T2, R]] = (new Untupled2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function1[Tuple3[T1, T2, T3], R]]) { def untupled3(): PackedSpork[Function3[T1, T2, T3, R]] = (new Untupled3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }

  extension [R](spork1: Spork[Function0[R]]) { def tupled0(): Spork[Function1[EmptyTuple, R]] = spork1.pack().tupled0().unpack() }
  extension [T1, R](spork1: Spork[Function1[T1, R]]) { def tupled1(): Spork[Function1[Tuple1[T1], R]] = spork1.pack().tupled1().unpack() }
  extension [T1, T2, R](spork1: Spork[Function2[T1, T2, R]]) { def tupled2(): Spork[Function1[Tuple2[T1, T2], R]] = spork1.pack().tupled2().unpack() }
  extension [T1, T2, T3, R](spork1: Spork[Function3[T1, T2, T3, R]]) { def tupled3(): Spork[Function1[Tuple3[T1, T2, T3], R]] = spork1.pack().tupled3().unpack() }
  extension [R](spork1: Spork[Function1[EmptyTuple, R]]) { def untupled0(): Spork[Function0[R]] = spork1.pack().untupled0().unpack() }
  extension [T1, R](spork1: Spork[Function1[Tuple1[T1], R]]) { def untupled1(): Spork[Function1[T1, R]] = spork1.pack().untupled1().unpack() }
  extension [T1, T2, R](spork1: Spork[Function1[Tuple2[T1, T2], R]]) { def untupled2(): Spork[Function2[T1, T2, R]] = spork1.pack().untupled2().unpack() }
  extension [T1, T2, T3, R](spork1: Spork[Function1[Tuple3[T1, T2, T3], R]]) { def untupled3(): Spork[Function3[T1, T2, T3, R]] = spork1.pack().untupled3().unpack() }

}
