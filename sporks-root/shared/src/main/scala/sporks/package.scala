package object sporks {

  import upickle.default.*

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[PackedSpork[T]]
  //////////////////////////////////////////////////////////////////////////////

  given [T]: ReadWriter[PackedSpork[T]]  = macroRW
  given [T]: ReadWriter[PackedObject[T]] = macroRW
  given [T]: ReadWriter[PackedClass[T]]  = macroRW
  given [T]: ReadWriter[PackedLambda[T]] = macroRW
  given [E, T]: ReadWriter[PackedWithEnv[E, T]] = macroRW
  given [E, T]: ReadWriter[PackedWithCtx[E, T]] = macroRW

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[T]] for primitive T
  //////////////////////////////////////////////////////////////////////////////

  object INT_RW extends SporkObject[ReadWriter[Int]](summon[ReadWriter[Int]])
  given int_rw: PackedSpork[ReadWriter[Int]] = INT_RW.pack()

  object STRING_RW extends SporkObject[ReadWriter[String]](summon[ReadWriter[String]])
  given str_rw: PackedSpork[ReadWriter[String]] = STRING_RW.pack()

  object BOOLEAN_RW extends SporkObject[ReadWriter[Boolean]](summon[ReadWriter[Boolean]])
  given bool_rw: PackedSpork[ReadWriter[Boolean]] = BOOLEAN_RW.pack()

  object DOUBLE_RW extends SporkObject[ReadWriter[Double]](summon[ReadWriter[Double]])
  given double_rw: PackedSpork[ReadWriter[Double]] = DOUBLE_RW.pack()

  object FLOAT_RW extends SporkObject[ReadWriter[Float]](summon[ReadWriter[Float]])
  given float_rw: PackedSpork[ReadWriter[Float]] = FLOAT_RW.pack()

  object LONG_RW extends SporkObject[ReadWriter[Long]](summon[ReadWriter[Long]])
  given long_rw: PackedSpork[ReadWriter[Long]] = LONG_RW.pack()

  object SHORT_RW extends SporkObject[ReadWriter[Short]](summon[ReadWriter[Short]])
  given short_rw: PackedSpork[ReadWriter[Short]] = SHORT_RW.pack()

  object BYTE_RW extends SporkObject[ReadWriter[Byte]](summon[ReadWriter[Byte]])
  given byte_rw: PackedSpork[ReadWriter[Byte]] = BYTE_RW.pack()

  object CHAR_RW extends SporkObject[ReadWriter[Char]](summon[ReadWriter[Char]])
  given char_rw: PackedSpork[ReadWriter[Char]] = CHAR_RW.pack()

  object UNIT_RW extends SporkObject[ReadWriter[Unit]](summon[ReadWriter[Unit]])
  given unit_rw: PackedSpork[ReadWriter[Unit]] = UNIT_RW.pack()

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[F[T]]] for Option[T], List[T], etc.
  //////////////////////////////////////////////////////////////////////////////

  // Note:
  // `PackedSpork[ReadWriter[PackedSpork[T]]]`s are implemented as
  // `SporkObject`s instead of `SporkClass`es. This is for optimization reasons,
  // a packed spork can use the same `ReadWriter` for all type parameters T. To
  // make it work, the object is cast to `PackedSpork[T]` using `asInstanceOf`.
  // This should have no effect on the runtime behavior, as the type parameter
  // T is erased.
  //
  // An alternative implementation with `SporkClass` would look like this:
  // class PACKED_RW_T[T] extends SporkClass[ReadWriter[PackedSpork[T]]](macroRW)
  // given packed_rw_t[T]: PackedSpork[ReadWriter[PackedSpork[T]]] = (new PACKED_RW_T()).pack()

  object PACKED_RW extends SporkObject[ReadWriter[PackedSpork[_]]](macroRW)
  given packed_rw[T]: PackedSpork[ReadWriter[PackedSpork[T]]] = PACKED_RW.pack().asInstanceOf

  object PACKED_OBJECT_RW extends SporkObject[ReadWriter[PackedObject[_]]](macroRW)
  given packed_object_rw[T]: PackedSpork[ReadWriter[PackedObject[T]]] = PACKED_OBJECT_RW.pack().asInstanceOf

  object PACKED_CLASS_RW extends SporkObject[ReadWriter[PackedClass[_]]](macroRW)
  given packed_class_rw[T]: PackedSpork[ReadWriter[PackedClass[T]]] = PACKED_CLASS_RW.pack().asInstanceOf

  object PACKED_LAMBDA_RW extends SporkObject[ReadWriter[PackedLambda[_]]](macroRW)
  given packed_lambda_rw[T]: PackedSpork[ReadWriter[PackedLambda[T]]] = PACKED_LAMBDA_RW.pack().asInstanceOf

  object PACKED_WITH_ENV_RW extends SporkObject[ReadWriter[PackedWithEnv[_, _]]](macroRW)
  given packed_with_env_rw[E, T]: PackedSpork[ReadWriter[PackedWithEnv[E, T]]] = PACKED_WITH_ENV_RW.pack().asInstanceOf

  object PACKED_WITH_CTX_RW extends SporkObject[ReadWriter[PackedWithCtx[_, _]]](macroRW)
  given packed_with_ctx_rw[E, T]: PackedSpork[ReadWriter[PackedWithCtx[E, T]]] = PACKED_WITH_CTX_RW.pack().asInstanceOf

  private abstract class UnpackingCombinator1[T1, R](fun: T1 ?=> R) extends SporkClass[PackedSpork[T1] ?=> R]({ packed ?=> fun.apply(using packed.build()) })
  private abstract class UnpackingCombinator2[T1, T2, U](fun: T1 ?=> T2 ?=> U) extends SporkClass[PackedSpork[T1] ?=> PackedSpork[T2] ?=> U]({ packed1 ?=> packed2 ?=> fun.apply(using packed1.build()).apply(using (packed2.build())) })
  private abstract class UnpackingCombinator3[T1, T2, T3, U](fun: T1 ?=> T2 ?=> T3 ?=> U) extends SporkClass[PackedSpork[T1] ?=> PackedSpork[T2] ?=> PackedSpork[T3] ?=> U]({ packed1 ?=> packed2 ?=> packed3 ?=> fun.apply(using packed1.build()).apply(using packed2.build()).apply(using packed3.build()) })

  class SOME_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[Some[T]]]({ summon[ReadWriter[Some[T]]] })
  given some_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Some[T]]] = new SOME_RW[T].pack().packWithCtx(t_rw)

  object NONE_RW extends SporkObject[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given none_rw: PackedSpork[ReadWriter[None.type]] = NONE_RW.pack()

  class OPTION_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[Option[T]]]({ summon[ReadWriter[Option[T]]] })
  given option_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Option[T]]] = (new OPTION_RW[T]).pack().packWithCtx(t_rw)

  class LIST_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[List[T]]]({ summon[ReadWriter[List[T]]] })
  given list_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[List[T]]] = new LIST_RW[T].pack().packWithCtx(t_rw)

  object TUPLE0_RW extends SporkObject[ReadWriter[EmptyTuple]](macroRW[EmptyTuple])
  given tuple0_rw: PackedSpork[ReadWriter[EmptyTuple]] = TUPLE0_RW.pack()

  class TUPLE1_RW[T1] extends UnpackingCombinator1[ReadWriter[T1], ReadWriter[Tuple1[T1]]]({ summon[ReadWriter[Tuple1[T1]]] })
  given packed_tuple1_rw[T1](using rw1: PackedSpork[ReadWriter[T1]]): PackedSpork[ReadWriter[Tuple1[T1]]] = (new TUPLE1_RW[T1]).pack().packWithCtx(rw1)

  class TUPLE2_RW[T1, T2] extends UnpackingCombinator2[ReadWriter[T1], ReadWriter[T2], ReadWriter[Tuple2[T1, T2]]]({ summon[ReadWriter[Tuple2[T1, T2]]] })
  given packed_tuple2_rw[T1, T2](using rw1: PackedSpork[ReadWriter[T1]], rw2: PackedSpork[ReadWriter[T2]]): PackedSpork[ReadWriter[Tuple2[T1, T2]]] = (new TUPLE2_RW[T1, T2]).pack().packWithCtx(rw1).packWithCtx(rw2)

  class TUPLE3_RW[T1, T2, T3] extends UnpackingCombinator3[ReadWriter[T1], ReadWriter[T2], ReadWriter[T3], ReadWriter[Tuple3[T1, T2, T3]]]({ summon[ReadWriter[Tuple3[T1, T2, T3]]] })
  given packed_tuple3_rw[T1, T2, T3](using rw1: PackedSpork[ReadWriter[T1]], rw2: PackedSpork[ReadWriter[T2]], rw3: PackedSpork[ReadWriter[T3]]): PackedSpork[ReadWriter[Tuple3[T1, T2, T3]]] = (new TUPLE3_RW[T1, T2, T3]).pack().packWithCtx(rw1).packWithCtx(rw2).packWithCtx(rw3)

  //////////////////////////////////////////////////////////////////////////////
  // tupledN(); untupledN()
  //////////////////////////////////////////////////////////////////////////////

  private def tupled0[R](fun1: Function0[R]): Function1[EmptyTuple, R] = { case EmptyTuple => fun1() }
  private def tupled1[T1, R](fun1: Function1[T1, R]): Function1[Tuple1[T1], R] = { case Tuple1(x1) => fun1(x1) }
  private def tupled2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[(T1, T2), R] = { case Tuple2(x1, x2) => fun1(x1, x2) }
  private def tupled3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[(T1, T2, T3), R] = { case Tuple3(x1, x2, x3) => fun1(x1, x2, x3) }
  private def untupled0[R](fun1: Function1[EmptyTuple, R]): Function0[R] = { () => fun1(EmptyTuple) }
  private def untupled1[T1, R](fun1: Function1[Tuple1[T1], R]): Function1[T1, R] = { (x1) => fun1(Tuple1(x1)) }
  private def untupled2[T1, T2, R](fun1: Function1[(T1, T2), R]): Function2[T1, T2, R] = { (x1, x2) => fun1(Tuple2(x1, x2)) }
  private def untupled3[T1, T2, T3, R](fun1: Function1[(T1, T2, T3), R]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(Tuple3(x1, x2, x3)) }
  private[sporks] class Tupled0[R] extends SporkClass[PackedSpork[Function0[R]] => Function1[EmptyTuple, R]]({ fun1 => tupled0(fun1.build()) })
  private[sporks] class Tupled1[T1, R] extends SporkClass[PackedSpork[Function1[T1, R]] => Function1[Tuple1[T1], R]]({ fun1 => tupled1(fun1.build()) })
  private[sporks] class Tupled2[T1, T2, R] extends SporkClass[PackedSpork[Function2[T1, T2, R]] => Function1[Tuple2[T1, T2], R]]({ fun1 => tupled2(fun1.build()) })
  private[sporks] class Tupled3[T1, T2, T3, R] extends SporkClass[PackedSpork[Function3[T1, T2, T3, R]] => Function1[Tuple3[T1, T2, T3], R]]({ fun1 => tupled3(fun1.build()) })
  private[sporks] class Untupled0[R] extends SporkClass[PackedSpork[Function1[EmptyTuple, R]] => Function0[R]]({ fun1 => untupled0(fun1.build()) })
  private[sporks] class Untupled1[T1, R] extends SporkClass[PackedSpork[Function1[Tuple1[T1], R]] => Function1[T1, R]]({ fun1 => untupled1(fun1.build()) })
  private[sporks] class Untupled2[T1, T2, R] extends SporkClass[PackedSpork[Function1[(T1, T2), R]] => Function2[T1, T2, R]]({ fun1 => untupled2(fun1.build()) })
  private[sporks] class Untupled3[T1, T2, T3, R] extends SporkClass[PackedSpork[Function1[(T1, T2, T3), R]] => Function3[T1, T2, T3, R]]({ fun1 => untupled3(fun1.build()) })
  extension [R](packed1: PackedSpork[Function0[R]]) { def tupled0(): PackedSpork[Function1[EmptyTuple, R]] = (new Tupled0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def tupled1(): PackedSpork[Function1[Tuple1[T1], R]] = (new Tupled1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function2[T1, T2, R]]) { def tupled2(): PackedSpork[Function1[Tuple2[T1, T2], R]] = (new Tupled2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function3[T1, T2, T3, R]]) { def tupled3(): PackedSpork[Function1[Tuple3[T1, T2, T3], R]] = (new Tupled3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }
  extension [R](packed1: PackedSpork[Function1[EmptyTuple, R]]) { def untupled0(): PackedSpork[Function0[R]] = (new Untupled0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[Tuple1[T1], R]]) { def untupled1(): PackedSpork[Function1[T1, R]] = (new Untupled1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function1[Tuple2[T1, T2], R]]) { def untupled2(): PackedSpork[Function2[T1, T2, R]] = (new Untupled2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function1[Tuple3[T1, T2, T3], R]]) { def untupled3(): PackedSpork[Function3[T1, T2, T3, R]] = (new Untupled3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }

  //////////////////////////////////////////////////////////////////////////////
  // curriedN(); uncurriedN()
  //////////////////////////////////////////////////////////////////////////////

  private def curried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def curried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def curried2[T1, T2, R](fun1: Function2[T1, T2, R]): Function1[T1, Function1[T2, R]] = { x1 => x2 => fun1(x1, x2) }
  private def curried3[T1, T2, T3, R](fun1: Function3[T1, T2, T3, R]): Function1[T1, Function1[T2, Function1[T3, R]]] = { x1 => x2 => x3 => fun1(x1, x2, x3) }
  private def uncurried0[R](fun1: Function0[R]): Function0[R] = { fun1 }
  private def uncurried1[T1, R](fun1: Function1[T1, R]): Function1[T1, R] = { fun1 }
  private def uncurried2[T1, T2, R](fun1: Function1[T1, Function1[T2, R]]): Function2[T1, T2, R] = { (x1, x2) => fun1(x1)(x2) }
  private def uncurried3[T1, T2, T3, R](fun1: Function1[T1, Function1[T2, Function1[T3, R]]]): Function3[T1, T2, T3, R] = { (x1, x2, x3) => fun1(x1)(x2)(x3) }
  private[sporks] class Curried0[R] extends SporkClass[PackedSpork[Function0[R]] => Function0[R]]({ fun1 => curried0(fun1.build()) })
  private[sporks] class Curried1[T1, R] extends SporkClass[PackedSpork[Function1[T1, R]] => Function1[T1, R]]({ fun1 => curried1(fun1.build()) })
  private[sporks] class Curried2[T1, T2, R] extends SporkClass[PackedSpork[Function2[T1, T2, R]] => Function1[T1, Function1[T2, R]]]({ fun1 => curried2(fun1.build()) })
  private[sporks] class Curried3[T1, T2, T3, R] extends SporkClass[PackedSpork[Function3[T1, T2, T3, R]] => Function1[T1, Function1[T2, Function1[T3, R]]]]({ fun1 => curried3(fun1.build()) })
  private[sporks] class Uncurried0[R] extends SporkClass[PackedSpork[Function0[R]] => Function0[R]]({ fun1 => uncurried0(fun1.build()) })
  private[sporks] class Uncurried1[T1, R] extends SporkClass[PackedSpork[Function1[T1, R]] => Function1[T1, R]]({ fun1 => uncurried1(fun1.build()) })
  private[sporks] class Uncurried2[T1, T2, R] extends SporkClass[PackedSpork[Function1[T1, Function1[T2, R]]] => Function2[T1, T2, R]]({ fun1 => uncurried2(fun1.build()) })
  private[sporks] class Uncurried3[T1, T2, T3, R] extends SporkClass[PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]] => Function3[T1, T2, T3, R]]({ fun1 => uncurried3(fun1.build()) })
  extension [R](packed1: PackedSpork[Function0[R]]) { def curried0(): PackedSpork[Function0[R]] = (new Curried0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def curried1(): PackedSpork[Function1[T1, R]] = (new Curried1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function2[T1, T2, R]]) { def curried2(): PackedSpork[Function1[T1, Function1[T2, R]]] = (new Curried2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function3[T1, T2, T3, R]]) { def curried3(): PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]] = (new Curried3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }
  extension [R](packed1: PackedSpork[Function0[R]]) { def uncurried0(): PackedSpork[Function0[R]] = (new Uncurried0[R]()).pack().packWithEnv(packed1) }
  extension [T1, R](packed1: PackedSpork[Function1[T1, R]]) { def uncurried1(): PackedSpork[Function1[T1, R]] = (new Uncurried1[T1, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, R](packed1: PackedSpork[Function1[T1, Function1[T2, R]]]) { def uncurried2(): PackedSpork[Function2[T1, T2, R]] = (new Uncurried2[T1, T2, R]()).pack().packWithEnv(packed1) }
  extension [T1, T2, T3, R](packed1: PackedSpork[Function1[T1, Function1[T2, Function1[T3, R]]]]) { def uncurried3(): PackedSpork[Function3[T1, T2, T3, R]] = (new Uncurried3[T1, T2, T3, R]()).pack().packWithEnv(packed1) }

}
