package sporks.extensions

import sporks.*
import sporks.given


object Tupled {

  private def tupled0[R](fun1: Function0[R]): Function1[EmptyTuple, R] = { case EmptyTuple => fun1() }
  private def tupled1[T1, R](fun1: Function1[T1, R]): Function1[Tuple1[T1], R] = { case Tuple1(x1) => fun1(x1) }
  private def tupled2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[(T1, T2), R] = { case Tuple2(x1, x2) => fun1(x1, x2) }
  private def tupled3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[(T1, T2, T3), R] = { case Tuple3(x1, x2, x3) => fun1(x1, x2, x3) }
  private def tupled4[T1, T2, T3, T4, R](fun1: Function4[T1, T2, T3, T4, R]): Function1[(T1, T2, T3, T4), R] = { case Tuple4(x1, x2, x3, x4) => fun1(x1, x2, x3, x4) }
  private def tupled5[T1, T2, T3, T4, T5, R](fun1: Function5[T1, T2, T3, T4, T5, R]): Function1[(T1, T2, T3, T4, T5), R] = { case Tuple5(x1, x2, x3, x4, x5) => fun1(x1, x2, x3, x4, x5) }
  private def tupled6[T1, T2, T3, T4, T5, T6, R](fun1: Function6[T1, T2, T3, T4, T5, T6, R]): Function1[(T1, T2, T3, T4, T5, T6), R] = { case Tuple6(x1, x2, x3, x4, x5, x6) => fun1(x1, x2, x3, x4, x5, x6) }
  private def tupled7[T1, T2, T3, T4, T5, T6, T7, R](fun1: Function7[T1, T2, T3, T4, T5, T6, T7, R]): Function1[(T1, T2, T3, T4, T5, T6, T7), R] = { case Tuple7(x1, x2, x3, x4, x5, x6, x7) => fun1(x1, x2, x3, x4, x5, x6, x7) }
  private def untupled0[R](fun1: Function1[EmptyTuple, R]): Function0[R] = { () => fun1(EmptyTuple) }
  private def untupled1[T1, R](fun1: Function1[Tuple1[T1], R]): Function1[T1, R] = { (x1) => fun1(Tuple1(x1)) }
  private def untupled2[T1, T2, R](fun1: Function1[(T1, T2), R]): Function2[T1, T2, R] = { (x1, x2) => fun1(Tuple2(x1, x2)) }
  private def untupled3[T1, T2, T3, R](fun1: Function1[(T1, T2, T3), R]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(Tuple3(x1, x2, x3)) }
  private def untupled4[T1, T2, T3, T4, R](fun1: Function1[(T1, T2, T3, T4), R]): Function4[T1, T2, T3, T4, R] = { (x1, x2, x3, x4) => fun1(Tuple4(x1, x2, x3, x4)) }
  private def untupled5[T1, T2, T3, T4, T5, R](fun1: Function1[(T1, T2, T3, T4, T5), R]): Function5[T1, T2, T3, T4, T5, R] = { (x1, x2, x3, x4, x5) => fun1(Tuple5(x1, x2, x3, x4, x5)) }
  private def untupled6[T1, T2, T3, T4, T5, T6, R](fun1: Function1[(T1, T2, T3, T4, T5, T6), R]): Function6[T1, T2, T3, T4, T5, T6, R] = { (x1, x2, x3, x4, x5, x6) => fun1(Tuple6(x1, x2, x3, x4, x5, x6)) }
  private def untupled7[T1, T2, T3, T4, T5, T6, T7, R](fun1: Function1[(T1, T2, T3, T4, T5, T6, T7), R]): Function7[T1, T2, T3, T4, T5, T6, T7, R] = { (x1, x2, x3, x4, x5, x6, x7) => fun1(Tuple7(x1, x2, x3, x4, x5, x6, x7)) }

  private[sporks] final class Tupled0[R] extends SporkClassBuilder[Function0[R] => Function1[EmptyTuple, R]]({ fun1 => tupled0(fun1) })
  private[sporks] final class Tupled1[T1, R] extends SporkClassBuilder[Function1[T1, R] => Function1[Tuple1[T1], R]]({ fun1 => tupled1(fun1) })
  private[sporks] final class Tupled2[T1, T2, R] extends SporkClassBuilder[Function2[T1, T2, R] => Function1[Tuple2[T1, T2], R]]({ fun1 => tupled2(fun1) })
  private[sporks] final class Tupled3[T1, T2, T3, R] extends SporkClassBuilder[Function3[T1, T2, T3, R] => Function1[Tuple3[T1, T2, T3], R]]({ fun1 => tupled3(fun1) })
  private[sporks] final class Tupled4[T1, T2, T3, T4, R] extends SporkClassBuilder[Function4[T1, T2, T3, T4, R] => Function1[(T1, T2, T3, T4), R]]({ fun1 => tupled4(fun1) })
  private[sporks] final class Tupled5[T1, T2, T3, T4, T5, R] extends SporkClassBuilder[Function5[T1, T2, T3, T4, T5, R] => Function1[(T1, T2, T3, T4, T5), R]]({ fun1 => tupled5(fun1) })
  private[sporks] final class Tupled6[T1, T2, T3, T4, T5, T6, R] extends SporkClassBuilder[Function6[T1, T2, T3, T4, T5, T6, R] => Function1[(T1, T2, T3, T4, T5, T6), R]]({ fun1 => tupled6(fun1) })
  private[sporks] final class Tupled7[T1, T2, T3, T4, T5, T6, T7, R] extends SporkClassBuilder[Function7[T1, T2, T3, T4, T5, T6, T7, R] => Function1[(T1, T2, T3, T4, T5, T6, T7), R]]({ fun1 => tupled7(fun1) })
  private[sporks] final class Untupled0[R] extends SporkClassBuilder[Function1[EmptyTuple, R] => Function0[R]]({ fun1 => untupled0(fun1) })
  private[sporks] final class Untupled1[T1, R] extends SporkClassBuilder[Function1[Tuple1[T1], R] => Function1[T1, R]]({ fun1 => untupled1(fun1) })
  private[sporks] final class Untupled2[T1, T2, R] extends SporkClassBuilder[Function1[(T1, T2), R] => Function2[T1, T2, R]]({ fun1 => untupled2(fun1) })
  private[sporks] final class Untupled3[T1, T2, T3, R] extends SporkClassBuilder[Function1[(T1, T2, T3), R] => Function3[T1, T2, T3, R]]({ fun1 => untupled3(fun1) })
  private[sporks] final class Untupled4[T1, T2, T3, T4, R] extends SporkClassBuilder[Function1[(T1, T2, T3, T4), R] => Function4[T1, T2, T3, T4, R]]({ fun1 => untupled4(fun1) })
  private[sporks] final class Untupled5[T1, T2, T3, T4, T5, R] extends SporkClassBuilder[Function1[(T1, T2, T3, T4, T5), R] => Function5[T1, T2, T3, T4, T5, R]]({ fun1 => untupled5(fun1) })
  private[sporks] final class Untupled6[T1, T2, T3, T4, T5, T6, R] extends SporkClassBuilder[Function1[(T1, T2, T3, T4, T5, T6), R] => Function6[T1, T2, T3, T4, T5, T6, R]]({ fun1 => untupled6(fun1) })
  private[sporks] final class Untupled7[T1, T2, T3, T4, T5, T6, T7, R] extends SporkClassBuilder[Function1[(T1, T2, T3, T4, T5, T6, T7), R] => Function7[T1, T2, T3, T4, T5, T6, T7, R]]({ fun1 => untupled7(fun1) })

  extension [R](packed1: Spork[Function0[R]]) { def tupled0(): Spork[Function1[EmptyTuple, R]] = (new Tupled0[R]()).pack().withEnv2(packed1) }
  extension [T1, R](packed1: Spork[Function1[T1, R]]) { def tupled1(): Spork[Function1[Tuple1[T1], R]] = (new Tupled1[T1, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, R](packed1: Spork[Function2[T1, T2, R]]) { def tupled2(): Spork[Function1[Tuple2[T1, T2], R]] = (new Tupled2[T1, T2, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, R](packed1: Spork[Function3[T1, T2, T3, R]]) { def tupled3(): Spork[Function1[Tuple3[T1, T2, T3], R]] = (new Tupled3[T1, T2, T3, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, R](packed1: Spork[Function4[T1, T2, T3, T4, R]]) { def tupled4(): Spork[Function1[(T1, T2, T3, T4), R]] = (new Tupled4[T1, T2, T3, T4, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, R](packed1: Spork[Function5[T1, T2, T3, T4, T5, R]]) { def tupled5(): Spork[Function1[(T1, T2, T3, T4, T5), R]] = (new Tupled5[T1, T2, T3, T4, T5, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, R](packed1: Spork[Function6[T1, T2, T3, T4, T5, T6, R]]) { def tupled6(): Spork[Function1[(T1, T2, T3, T4, T5, T6), R]] = (new Tupled6[T1, T2, T3, T4, T5, T6, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](packed1: Spork[Function7[T1, T2, T3, T4, T5, T6, T7, R]]) { def tupled7(): Spork[Function1[(T1, T2, T3, T4, T5, T6, T7), R]] = (new Tupled7[T1, T2, T3, T4, T5, T6, T7, R]()).pack().withEnv2(packed1) }
  extension [R](packed1: Spork[Function1[EmptyTuple, R]]) { def untupled0(): Spork[Function0[R]] = (new Untupled0[R]()).pack().withEnv2(packed1) }
  extension [T1, R](packed1: Spork[Function1[Tuple1[T1], R]]) { def untupled1(): Spork[Function1[T1, R]] = (new Untupled1[T1, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, R](packed1: Spork[Function1[Tuple2[T1, T2], R]]) { def untupled2(): Spork[Function2[T1, T2, R]] = (new Untupled2[T1, T2, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, R](packed1: Spork[Function1[Tuple3[T1, T2, T3], R]]) { def untupled3(): Spork[Function3[T1, T2, T3, R]] = (new Untupled3[T1, T2, T3, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, R](packed1: Spork[Function1[Tuple4[T1, T2, T3, T4], R]]) { def untupled4(): Spork[Function4[T1, T2, T3, T4, R]] = (new Untupled4[T1, T2, T3, T4, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, R](packed1: Spork[Function1[Tuple5[T1, T2, T3, T4, T5], R]]) { def untupled5(): Spork[Function5[T1, T2, T3, T4, T5, R]] = (new Untupled5[T1, T2, T3, T4, T5, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, R](packed1: Spork[Function1[Tuple6[T1, T2, T3, T4, T5, T6], R]]) { def untupled6(): Spork[Function6[T1, T2, T3, T4, T5, T6, R]] = (new Untupled6[T1, T2, T3, T4, T5, T6, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](packed1: Spork[Function1[Tuple7[T1, T2, T3, T4, T5, T6, T7], R]]) { def untupled7(): Spork[Function7[T1, T2, T3, T4, T5, T6, T7, R]] = (new Untupled7[T1, T2, T3, T4, T5, T6, T7, R]()).pack().withEnv2(packed1) }

}
