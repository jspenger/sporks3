package sporks

private[sporks] object ReadWriters {

  import upickle.default.*

  import sporks.*
  import sporks.Spork.*
  import sporks.PackedSpork.*

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[PackedSpork[T]]
  //////////////////////////////////////////////////////////////////////////////

  given [T]: ReadWriter[PackedSpork[T]]  = macroRW
  given [T]: ReadWriter[PackedObject[T]] = macroRW
  given [T]: ReadWriter[PackedClass[T]]  = macroRW
  given [T]: ReadWriter[PackedLambda[T]] = macroRW
  given [E]: ReadWriter[PackedEnv[E]]    = macroRW
  given [E, T]: ReadWriter[PackedWithEnv[E, T]] = macroRW
  given [E, T]: ReadWriter[PackedWithCtx[E, T]] = macroRW

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[T]] for primitive T
  //////////////////////////////////////////////////////////////////////////////

  private[sporks] object INT_RW extends SporkObjectBuilder[ReadWriter[Int]](summon[ReadWriter[Int]])
  given int_rw: PackedSpork[ReadWriter[Int]] = INT_RW.pack()

  private[sporks] object STRING_RW extends SporkObjectBuilder[ReadWriter[String]](summon[ReadWriter[String]])
  given str_rw: PackedSpork[ReadWriter[String]] = STRING_RW.pack()

  private[sporks] object BOOLEAN_RW extends SporkObjectBuilder[ReadWriter[Boolean]](summon[ReadWriter[Boolean]])
  given bool_rw: PackedSpork[ReadWriter[Boolean]] = BOOLEAN_RW.pack()

  private[sporks] object DOUBLE_RW extends SporkObjectBuilder[ReadWriter[Double]](summon[ReadWriter[Double]])
  given double_rw: PackedSpork[ReadWriter[Double]] = DOUBLE_RW.pack()

  private[sporks] object FLOAT_RW extends SporkObjectBuilder[ReadWriter[Float]](summon[ReadWriter[Float]])
  given float_rw: PackedSpork[ReadWriter[Float]] = FLOAT_RW.pack()

  private[sporks] object LONG_RW extends SporkObjectBuilder[ReadWriter[Long]](summon[ReadWriter[Long]])
  given long_rw: PackedSpork[ReadWriter[Long]] = LONG_RW.pack()

  private[sporks] object SHORT_RW extends SporkObjectBuilder[ReadWriter[Short]](summon[ReadWriter[Short]])
  given short_rw: PackedSpork[ReadWriter[Short]] = SHORT_RW.pack()

  private[sporks] object BYTE_RW extends SporkObjectBuilder[ReadWriter[Byte]](summon[ReadWriter[Byte]])
  given byte_rw: PackedSpork[ReadWriter[Byte]] = BYTE_RW.pack()

  private[sporks] object CHAR_RW extends SporkObjectBuilder[ReadWriter[Char]](summon[ReadWriter[Char]])
  given char_rw: PackedSpork[ReadWriter[Char]] = CHAR_RW.pack()

  private[sporks] object UNIT_RW extends SporkObjectBuilder[ReadWriter[Unit]](summon[ReadWriter[Unit]])
  given unit_rw: PackedSpork[ReadWriter[Unit]] = UNIT_RW.pack()

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[F[T]]] for Option[T], List[T], etc.
  //////////////////////////////////////////////////////////////////////////////

  // Note:
  // `PackedSpork[ReadWriter[PackedSpork[T]]]`s are implemented as
  // `SporkObjectBuilder`s instead of `SporkClassBuilder`es. This is for optimization reasons,
  // a packed spork can use the same `ReadWriter` for all type parameters T. To
  // make it work, the object is cast to `PackedSpork[T]` using `asInstanceOf`.
  // This should have no effect on the runtime behavior, as the type parameter
  // T is erased.
  //
  // An alternative implementation with `SporkClassBuilder` would look like this:
  // class PACKED_RW_T[T] extends SporkClassBuilder[ReadWriter[PackedSpork[T]]](macroRW)
  // given packed_rw_t[T]: PackedSpork[ReadWriter[PackedSpork[T]]] = (new PACKED_RW_T()).pack()

  private[sporks] object PACKED_RW extends SporkObjectBuilder[ReadWriter[PackedSpork[_]]](macroRW)
  given packed_rw[T]: PackedSpork[ReadWriter[PackedSpork[T]]] = PACKED_RW.pack().asInstanceOf

  private[sporks] object PACKED_OBJECT_RW extends SporkObjectBuilder[ReadWriter[PackedObject[_]]](macroRW)
  given packed_object_rw[T]: PackedSpork[ReadWriter[PackedObject[T]]] = PACKED_OBJECT_RW.pack().asInstanceOf

  private[sporks] object PACKED_CLASS_RW extends SporkObjectBuilder[ReadWriter[PackedClass[_]]](macroRW)
  given packed_class_rw[T]: PackedSpork[ReadWriter[PackedClass[T]]] = PACKED_CLASS_RW.pack().asInstanceOf

  private[sporks] object PACKED_LAMBDA_RW extends SporkObjectBuilder[ReadWriter[PackedLambda[_]]](macroRW)
  given packed_lambda_rw[T]: PackedSpork[ReadWriter[PackedLambda[T]]] = PACKED_LAMBDA_RW.pack().asInstanceOf

  private[sporks] object PACKED_ENV_RW extends SporkObjectBuilder[ReadWriter[PackedEnv[_]]](macroRW)
  given packed_env_rw[E]: PackedSpork[ReadWriter[PackedEnv[E]]] = PACKED_ENV_RW.pack().asInstanceOf

  private[sporks] object PACKED_WITH_ENV_RW extends SporkObjectBuilder[ReadWriter[PackedWithEnv[_, _]]](macroRW)
  given packed_with_env_rw[E, T]: PackedSpork[ReadWriter[PackedWithEnv[E, T]]] = PACKED_WITH_ENV_RW.pack().asInstanceOf

  private[sporks] object PACKED_WITH_CTX_RW extends SporkObjectBuilder[ReadWriter[PackedWithCtx[_, _]]](macroRW)
  given packed_with_ctx_rw[E, T]: PackedSpork[ReadWriter[PackedWithCtx[E, T]]] = PACKED_WITH_CTX_RW.pack().asInstanceOf

  private[sporks] abstract class UnpackingCombinator1[T1, R](fun: T1 ?=> R) extends SporkClassBuilder[PackedSpork[T1] ?=> R]({ packed ?=> fun.apply(using packed.unwrap()) })
  private[sporks] abstract class UnpackingCombinator2[T1, T2, U](fun: T1 ?=> T2 ?=> U) extends SporkClassBuilder[PackedSpork[T1] ?=> PackedSpork[T2] ?=> U]({ packed1 ?=> packed2 ?=> fun.apply(using packed1.unwrap()).apply(using (packed2.unwrap())) })
  private[sporks] abstract class UnpackingCombinator3[T1, T2, T3, U](fun: T1 ?=> T2 ?=> T3 ?=> U) extends SporkClassBuilder[PackedSpork[T1] ?=> PackedSpork[T2] ?=> PackedSpork[T3] ?=> U]({ packed1 ?=> packed2 ?=> packed3 ?=> fun.apply(using packed1.unwrap()).apply(using packed2.unwrap()).apply(using packed3.unwrap()) })

  private[sporks] class SOME_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[Some[T]]]({ summon[ReadWriter[Some[T]]] })
  given some_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Some[T]]] = new SOME_RW[T].pack().packWithCtx(t_rw)

  private[sporks] object NONE_RW extends SporkObjectBuilder[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given none_rw: PackedSpork[ReadWriter[None.type]] = NONE_RW.pack()

  private[sporks] class OPTION_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[Option[T]]]({ summon[ReadWriter[Option[T]]] })
  given option_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Option[T]]] = (new OPTION_RW[T]).pack().packWithCtx(t_rw)

  private[sporks] class LIST_RW[T] extends UnpackingCombinator1[ReadWriter[T], ReadWriter[List[T]]]({ summon[ReadWriter[List[T]]] })
  given list_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[List[T]]] = new LIST_RW[T].pack().packWithCtx(t_rw)

  private[sporks] object TUPLE0_RW extends SporkObjectBuilder[ReadWriter[EmptyTuple]](macroRW[EmptyTuple])
  given tuple0_rw: PackedSpork[ReadWriter[EmptyTuple]] = TUPLE0_RW.pack()

  private[sporks] class TUPLE1_RW[T1] extends UnpackingCombinator1[ReadWriter[T1], ReadWriter[Tuple1[T1]]]({ summon[ReadWriter[Tuple1[T1]]] })
  given packed_tuple1_rw[T1](using rw1: PackedSpork[ReadWriter[T1]]): PackedSpork[ReadWriter[Tuple1[T1]]] = (new TUPLE1_RW[T1]).pack().packWithCtx(rw1)

  private[sporks] class TUPLE2_RW[T1, T2] extends UnpackingCombinator2[ReadWriter[T1], ReadWriter[T2], ReadWriter[Tuple2[T1, T2]]]({ summon[ReadWriter[Tuple2[T1, T2]]] })
  given packed_tuple2_rw[T1, T2](using rw1: PackedSpork[ReadWriter[T1]], rw2: PackedSpork[ReadWriter[T2]]): PackedSpork[ReadWriter[Tuple2[T1, T2]]] = (new TUPLE2_RW[T1, T2]).pack().packWithCtx(rw1).packWithCtx(rw2)

  private[sporks] class TUPLE3_RW[T1, T2, T3] extends UnpackingCombinator3[ReadWriter[T1], ReadWriter[T2], ReadWriter[T3], ReadWriter[Tuple3[T1, T2, T3]]]({ summon[ReadWriter[Tuple3[T1, T2, T3]]] })
  given packed_tuple3_rw[T1, T2, T3](using rw1: PackedSpork[ReadWriter[T1]], rw2: PackedSpork[ReadWriter[T2]], rw3: PackedSpork[ReadWriter[T3]]): PackedSpork[ReadWriter[Tuple3[T1, T2, T3]]] = (new TUPLE3_RW[T1, T2, T3]).pack().packWithCtx(rw1).packWithCtx(rw2).packWithCtx(rw3)

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[Spork[T]]
  //////////////////////////////////////////////////////////////////////////////

  given spork_rw2[T]: ReadWriter[Spork[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack())
  given spork_obj_rw2[T]: ReadWriter[SporkObject[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkObject[T]]]
  given spork_cls_rw2[T]: ReadWriter[SporkClass[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkClass[T]]]
  given spork_lambda_rw2[T]: ReadWriter[SporkLambda[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkLambda[T]]]
  given spork_env_rw2[T]: ReadWriter[SporkEnv[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkEnv[T]]]
  given spork_with_env_rw2[E, T]: ReadWriter[SporkWithEnv[E, T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkWithEnv[E, T]]]
  given spork_with_ctx_rw2[E, T]: ReadWriter[SporkWithCtx[E, T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkWithCtx[E, T]]]

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[Spork[T]]]
  //////////////////////////////////////////////////////////////////////////////

  // Warning: Be careful to not use `summon` instead of `spork_rw2` here, as it
  // has caused issues in the past. Only do so if testing that other projects
  // don't break.

  private[sporks] object SPORK_RW extends SporkObjectBuilder[ReadWriter[Spork[_]]](spork_rw2.asInstanceOf)
  given spork_rw[T]: PackedSpork[ReadWriter[Spork[T]]] = SPORK_RW.pack().asInstanceOf[PackedSpork[ReadWriter[Spork[T]]]]

  private[sporks] object SPORK_OBJECT_RW extends SporkObjectBuilder[ReadWriter[SporkObject[_]]](spork_obj_rw2.asInstanceOf)
  given spork_object_rw[T]: PackedSpork[ReadWriter[SporkObject[T]]] = SPORK_OBJECT_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkObject[T]]]]

  private[sporks] object SPORK_CLASS_RW extends SporkObjectBuilder[ReadWriter[SporkClass[_]]](spork_cls_rw2.asInstanceOf)
  given spork_class_rw[T]: PackedSpork[ReadWriter[SporkClass[T]]] = SPORK_CLASS_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkClass[T]]]]

  private[sporks] object SPORK_LAMBDA_RW extends SporkObjectBuilder[ReadWriter[SporkLambda[_]]](spork_lambda_rw2.asInstanceOf)
  given spork_lambda_rw[T]: PackedSpork[ReadWriter[SporkLambda[T]]] = SPORK_LAMBDA_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkLambda[T]]]]

  private[sporks] object SPORK_ENV_RW extends SporkObjectBuilder[ReadWriter[SporkEnv[_]]](spork_env_rw2.asInstanceOf)
  given spork_env_rw[T]: PackedSpork[ReadWriter[SporkEnv[T]]] = SPORK_ENV_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkEnv[T]]]]

  private[sporks] object SPORK_WITH_ENV_RW extends SporkObjectBuilder[ReadWriter[SporkWithEnv[_, _]]](spork_with_env_rw2.asInstanceOf)
  given spork_with_env_rw[E, T]: PackedSpork[ReadWriter[SporkWithEnv[E, T]]] = SPORK_WITH_ENV_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkWithEnv[E, T]]]]

  private[sporks] object SPORK_WITH_CTX_RW extends SporkObjectBuilder[ReadWriter[SporkWithCtx[_, _]]](spork_with_ctx_rw2.asInstanceOf)
  given spork_with_ctx_rw[E, T]: PackedSpork[ReadWriter[SporkWithCtx[E, T]]] = SPORK_WITH_CTX_RW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkWithCtx[E, T]]]]

  //////////////////////////////////////////////////////////////////////////////
  // Spork[ReadWriter[T]] for T when PackedSpork[ReadWriter[T]] exists
  //////////////////////////////////////////////////////////////////////////////

  given spork_rw_from_packed[T](using PackedSpork[ReadWriter[T]]): Spork[ReadWriter[T]] = summon[PackedSpork[ReadWriter[T]]].unpack()

}
