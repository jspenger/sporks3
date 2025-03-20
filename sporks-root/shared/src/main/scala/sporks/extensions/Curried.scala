package sporks.extensions

import sporks.*
import sporks.given


object Curried {

  private def curried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def curried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def curried2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[T1, Function1[T2, R]] = { x1 => x2 => fun1(x1, x2) }
  private def curried3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[T1, Function1[T2, Function1[T3, R]]] = { x1 => x2 => x3 => fun1(x1, x2, x3) }
  private def uncurried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def uncurried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def uncurried2[T1, T2, R](fun1: Function1[T1, Function1[T2, R]]): Function2[T1, T2, R] = { (x1, x2) => fun1(x1)(x2) }
  private def uncurried3[T1, T2, T3, R](fun1: Function1[T1, Function1[T2, Function1[T3, R]]]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(x1)(x2)(x3) }

  private[sporks] class Curried0[R] extends SporkClassBuilder[PackedSpork[Function0[R]] => Function0[R]]({ fun1 => curried0(fun1.unwrap()) })
  private[sporks] class Curried1[T1, R] extends SporkClassBuilder[PackedSpork[Function1[T1, R]] => Function1[T1, R]]({ fun1 => curried1(fun1.unwrap()) })
  private[sporks] class Curried2[T1, T2, R] extends SporkClassBuilder[PackedSpork[Function2[T1, T2, R]] => Function1[T1, Function1[T2, R]]]({ fun1 => curried2(fun1.unwrap()) })
  private[sporks] class Curried3[T1, T2, T3, R] extends SporkClassBuilder[PackedSpork[Function3[T1, T2, T3, R]] => Function1[T1, Function1[T2, Function1[T3, R]]]]({ fun1 => curried3(fun1.unwrap()) })
  private[sporks] class Uncurried0[R] extends SporkClassBuilder[PackedSpork[Function0[R]] => Function0[R]]({ fun1 => uncurried0(fun1.unwrap()) })
  private[sporks] class Uncurried1[T1, R] extends SporkClassBuilder[PackedSpork[Function1[T1, R]] => Function1[T1, R]]({ fun1 => uncurried1(fun1.unwrap()) })
  private[sporks] class Uncurried2[T1, T2, R] extends SporkClassBuilder[PackedSpork[Function1[T1, Function1[T2, R]]] => Function2[T1, T2, R]]({ fun1 => uncurried2(fun1.unwrap()) })
  private[sporks] class Uncurried3[T1, T2, T3, R] extends SporkClassBuilder[PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]] => Function3[T1, T2, T3, R]]({ fun1 => uncurried3(fun1.unwrap()) })

  extension [R](packed1: PackedSpork[Function0[R]]) { def curried0(): PackedSpork[Function0[R]] = (new Curried0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def curried1(): PackedSpork[Function1[T1, R]] = (new Curried1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function2[T1, T2, R]]) { def curried2(): PackedSpork[Function1[T1, Function1[T2, R]]] = (new Curried2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function3[T1, T2, T3, R]]) { def curried3(): PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]] = (new Curried3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }
  extension [R](packed1: PackedSpork[Function0[R]]) { def uncurried0(): PackedSpork[Function0[R]] = (new Uncurried0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def uncurried1(): PackedSpork[Function1[T1, R]] = (new Uncurried1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function1[T1, Function1[T2, R]]]) { def uncurried2(): PackedSpork[Function2[T1, T2, R]] = (new Uncurried2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]]) { def uncurried3(): PackedSpork[Function3[T1, T2, T3, R]] = (new Uncurried3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }

  extension [R](spork1: Spork[Function0[R]]) { def curried02(): Spork[Function0[R]] = spork1.pack().curried0().unpack() }
  extension [T1, R](spork1: Spork[Function1[T1, R]]) { def curried12(): Spork[Function1[T1, R]] = spork1.pack().curried1().unpack() }
  extension [T1, T2, R](spork1: Spork[Function2[T1, T2, R]]) { def curried22(): Spork[Function1[T1, Function1[T2, R]]] = spork1.pack().curried2().unpack() }
  extension [T1, T2, T3, R](spork1: Spork[Function3[T1, T2, T3, R]]) { def curried32(): Spork[Function1[T1, Function1[T2, Function1[T3, R]]]] = spork1.pack().curried3().unpack() }
  extension [R](spork1: Spork[Function0[R]]) { def uncurried02(): Spork[Function0[R]] = spork1.pack().uncurried0().unpack() }
  extension [T1, R](spork1: Spork[Function1[T1, R]]) { def uncurried12(): Spork[Function1[T1, R]] = spork1.pack().uncurried1().unpack() }
  extension [T1, T2, R](spork1: Spork[Function1[T1, Function1[T2, R]]]) { def uncurried22(): Spork[Function2[T1, T2, R]] = spork1.pack().uncurried2().unpack() }
  extension [T1, T2, T3, R](spork1: Spork[Function1[T1, Function1[T2, Function1[T3, R]]]]) { def uncurried32(): Spork[Function3[T1, T2, T3, R]] = spork1.pack().uncurried3().unpack() }

}
