package sporks.extensions

import sporks.*
import sporks.given


object Curried {

  private def curried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def curried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def curried2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[T1, Function1[T2, R]] = { x1 => x2 => fun1(x1, x2) }
  private def curried3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[T1, Function1[T2, Function1[T3, R]]] = { x1 => x2 => x3 => fun1(x1, x2, x3) }
  private def curried4[T1, T2, T3, T4, R](fun1: Function4[T1, T2, T3, T4, R]): Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]] = { x1 => x2 => x3 => x4 => fun1(x1, x2, x3, x4) }
  private def curried5[T1, T2, T3, T4, T5, R](fun1: Function5[T1, T2, T3, T4, T5, R]): Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]] = { x1 => x2 => x3 => x4 => x5 => fun1(x1, x2, x3, x4, x5) }
  private def curried6[T1, T2, T3, T4, T5, T6, R](fun1: Function6[T1, T2, T3, T4, T5, T6, R]): Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]] = { x1 => x2 => x3 => x4 => x5 => x6 => fun1(x1, x2, x3, x4, x5, x6) }
  private def curried7[T1, T2, T3, T4, T5, T6, T7, R](fun1: Function7[T1, T2, T3, T4, T5, T6, T7, R]): Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]] = { x1 => x2 => x3 => x4 => x5 => x6 => x7 => fun1(x1, x2, x3, x4, x5, x6, x7) }
  private def uncurried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def uncurried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def uncurried2[T1, T2, R](fun1: Function1[T1, Function1[T2, R]]): Function2[T1, T2, R] = { (x1, x2) => fun1(x1)(x2) }
  private def uncurried3[T1, T2, T3, R](fun1: Function1[T1, Function1[T2, Function1[T3, R]]]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(x1)(x2)(x3) }
  private def uncurried4[T1, T2, T3, T4, R](fun1: Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]]): Function4[T1, T2, T3, T4, R] = { (x1, x2, x3, x4) => fun1(x1)(x2)(x3)(x4) }
  private def uncurried5[T1, T2, T3, T4, T5, R](fun1: Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]]): Function5[T1, T2, T3, T4, T5, R] = { (x1, x2, x3, x4, x5) => fun1(x1)(x2)(x3)(x4)(x5) }
  private def uncurried6[T1, T2, T3, T4, T5, T6, R](fun1: Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]]): Function6[T1, T2, T3, T4, T5, T6, R] = { (x1, x2, x3, x4, x5, x6) => fun1(x1)(x2)(x3)(x4)(x5)(x6) }
  private def uncurried7[T1, T2, T3, T4, T5, T6, T7, R](fun1: Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]]): Function7[T1, T2, T3, T4, T5, T6, T7, R] = { (x1, x2, x3, x4, x5, x6, x7) => fun1(x1)(x2)(x3)(x4)(x5)(x6)(x7) }

  private[sporks] final class Curried0[R] extends SporkClassBuilder[Function0[R] => Function0[R]]({ fun1 => curried0(fun1) })
  private[sporks] final class Curried1[T1, R] extends SporkClassBuilder[Function1[T1, R] => Function1[T1, R]]({ fun1 => curried1(fun1) })
  private[sporks] final class Curried2[T1, T2, R] extends SporkClassBuilder[Function2[T1, T2, R] => Function1[T1, Function1[T2, R]]]({ fun1 => curried2(fun1) })
  private[sporks] final class Curried3[T1, T2, T3, R] extends SporkClassBuilder[Function3[T1, T2, T3, R] => Function1[T1, Function1[T2, Function1[T3, R]]]]({ fun1 => curried3(fun1) })
  private[sporks] final class Curried4[T1, T2, T3, T4, R] extends SporkClassBuilder[Function4[T1, T2, T3, T4, R] => Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]]]({ fun1 => curried4(fun1) })
  private[sporks] final class Curried5[T1, T2, T3, T4, T5, R] extends SporkClassBuilder[Function5[T1, T2, T3, T4, T5, R] => Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]]]({ fun1 => curried5(fun1) })
  private[sporks] final class Curried6[T1, T2, T3, T4, T5, T6, R] extends SporkClassBuilder[Function6[T1, T2, T3, T4, T5, T6, R] => Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]]]({ fun1 => curried6(fun1) })
  private[sporks] final class Curried7[T1, T2, T3, T4, T5, T6, T7, R] extends SporkClassBuilder[Function7[T1, T2, T3, T4, T5, T6, T7, R] => Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]]]({ fun1 => curried7(fun1) })
  private[sporks] final class Uncurried0[R] extends SporkClassBuilder[Function0[R] => Function0[R]]({ fun1 => uncurried0(fun1) })
  private[sporks] final class Uncurried1[T1, R] extends SporkClassBuilder[Function1[T1, R] => Function1[T1, R]]({ fun1 => uncurried1(fun1) })
  private[sporks] final class Uncurried2[T1, T2, R] extends SporkClassBuilder[Function1[T1, Function1[T2, R]] => Function2[T1, T2, R]]({ fun1 => uncurried2(fun1) })
  private[sporks] final class Uncurried3[T1, T2, T3, R] extends SporkClassBuilder[Function1[T1, Function1[T2, Function1[T3, R]]] => Function3[T1, T2, T3, R]]({ fun1 => uncurried3(fun1) })
  private[sporks] final class Uncurried4[T1, T2, T3, T4, R] extends SporkClassBuilder[Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]] => Function4[T1, T2, T3, T4, R]]({ fun1 => uncurried4(fun1) })
  private[sporks] final class Uncurried5[T1, T2, T3, T4, T5, R] extends SporkClassBuilder[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]] => Function5[T1, T2, T3, T4, T5, R]]({ fun1 => uncurried5(fun1) })
  private[sporks] final class Uncurried6[T1, T2, T3, T4, T5, T6, R] extends SporkClassBuilder[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]] => Function6[T1, T2, T3, T4, T5, T6, R]]({ fun1 => uncurried6(fun1) })
  private[sporks] final class Uncurried7[T1, T2, T3, T4, T5, T6, T7, R] extends SporkClassBuilder[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]] => Function7[T1, T2, T3, T4, T5, T6, T7, R]]({ fun1 => uncurried7(fun1) })

  extension [R](packed1: Spork[Function0[R]]) { def curried0(): Spork[Function0[R]] = (new Curried0[R]()).pack().withEnv2(packed1) }
  extension [T1, R](packed1: Spork[Function1[T1, R]]) { def curried1(): Spork[Function1[T1, R]] = (new Curried1[T1, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, R](packed1: Spork[Function2[T1, T2, R]]) { def curried2(): Spork[Function1[T1, Function1[T2, R]]] = (new Curried2[T1, T2, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, R](packed1: Spork[Function3[T1, T2, T3, R]]) { def curried3(): Spork[Function1[T1, Function1[T2, Function1[T3, R]]]] = (new Curried3[T1, T2, T3, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, R](packed1: Spork[Function4[T1, T2, T3, T4, R]]) { def curried4(): Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]]] = (new Curried4[T1, T2, T3, T4, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, R](packed1: Spork[Function5[T1, T2, T3, T4, T5, R]]) { def curried5(): Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]]] = (new Curried5[T1, T2, T3, T4, T5, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, R](packed1: Spork[Function6[T1, T2, T3, T4, T5, T6, R]]) { def curried6(): Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]]] = (new Curried6[T1, T2, T3, T4, T5, T6, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](packed1: Spork[Function7[T1, T2, T3, T4, T5, T6, T7, R]]) { def curried7(): Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]]] = (new Curried7[T1, T2, T3, T4, T5, T6, T7, R]()).pack().withEnv2(packed1) }
  extension [R](packed1: Spork[Function0[R]]) { def uncurried0(): Spork[Function0[R]] = (new Uncurried0[R]()).pack().withEnv2(packed1) }
  extension [T1, R](packed1: Spork[Function1[T1, R]]) { def uncurried1(): Spork[Function1[T1, R]] = (new Uncurried1[T1, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, R](packed1: Spork[Function1[T1, Function1[T2, R]]]) { def uncurried2(): Spork[Function2[T1, T2, R]] = (new Uncurried2[T1, T2, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, R](packed1: Spork[Function1[T1, Function1[T2, Function1[T3, R]]]]) { def uncurried3(): Spork[Function3[T1, T2, T3, R]] = (new Uncurried3[T1, T2, T3, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, R](packed1: Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, R]]]]]) { def uncurried4(): Spork[Function4[T1, T2, T3, T4, R]] = (new Uncurried4[T1, T2, T3, T4, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, R](packed1: Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, R]]]]]]) { def uncurried5(): Spork[Function5[T1, T2, T3, T4, T5, R]] = (new Uncurried5[T1, T2, T3, T4, T5, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, R](packed1: Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, R]]]]]]]) { def uncurried6(): Spork[Function6[T1, T2, T3, T4, T5, T6, R]] = (new Uncurried6[T1, T2, T3, T4, T5, T6, R]()).pack().withEnv2(packed1) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](packed1: Spork[Function1[T1, Function1[T2, Function1[T3, Function1[T4, Function1[T5, Function1[T6, Function1[T7, R]]]]]]]]) { def uncurried7(): Spork[Function7[T1, T2, T3, T4, T5, T6, T7, R]] = (new Uncurried7[T1, T2, T3, T4, T5, T6, T7, R]()).pack().withEnv2(packed1) }

}
