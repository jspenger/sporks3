package sporks

object ReadWriters {

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

  private[sporks] object IntRW extends SporkObjectBuilder[ReadWriter[Int]](summon[ReadWriter[Int]])
  given intRW: PackedSpork[ReadWriter[Int]] = IntRW.pack()

  private[sporks] object StringRW extends SporkObjectBuilder[ReadWriter[String]](summon[ReadWriter[String]])
  given strRW: PackedSpork[ReadWriter[String]] = StringRW.pack()

  private[sporks] object BooleanRW extends SporkObjectBuilder[ReadWriter[Boolean]](summon[ReadWriter[Boolean]])
  given boolRW: PackedSpork[ReadWriter[Boolean]] = BooleanRW.pack()

  private[sporks] object DoubleRW extends SporkObjectBuilder[ReadWriter[Double]](summon[ReadWriter[Double]])
  given doubleRW: PackedSpork[ReadWriter[Double]] = DoubleRW.pack()

  private[sporks] object FloatRW extends SporkObjectBuilder[ReadWriter[Float]](summon[ReadWriter[Float]])
  given floatRW: PackedSpork[ReadWriter[Float]] = FloatRW.pack()

  private[sporks] object LongRW extends SporkObjectBuilder[ReadWriter[Long]](summon[ReadWriter[Long]])
  given longRW: PackedSpork[ReadWriter[Long]] = LongRW.pack()

  private[sporks] object ShortRW extends SporkObjectBuilder[ReadWriter[Short]](summon[ReadWriter[Short]])
  given shortRW: PackedSpork[ReadWriter[Short]] = ShortRW.pack()

  private[sporks] object ByteRW extends SporkObjectBuilder[ReadWriter[Byte]](summon[ReadWriter[Byte]])
  given byteRW: PackedSpork[ReadWriter[Byte]] = ByteRW.pack()

  private[sporks] object CharRW extends SporkObjectBuilder[ReadWriter[Char]](summon[ReadWriter[Char]])
  given charRW: PackedSpork[ReadWriter[Char]] = CharRW.pack()

  private[sporks] object UnitRW extends SporkObjectBuilder[ReadWriter[Unit]](summon[ReadWriter[Unit]])
  given unitRW: PackedSpork[ReadWriter[Unit]] = UnitRW.pack()

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[PackedSpork[_]]]
  //////////////////////////////////////////////////////////////////////////////

  // Note:`PackedSpork[ReadWriter[PackedSpork[T]]]`s are implemented as
  // `SporkObjectBuilder`s instead of `SporkClassBuilder`es. This is for
  // optimization reasons,

  private[sporks] object PackedSporkRW extends SporkObjectBuilder[ReadWriter[PackedSpork[_]]](macroRW)
  given packedSporkRW[T]: PackedSpork[ReadWriter[PackedSpork[T]]] = PackedSporkRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedSpork[T]]]]

  private[sporks] object PackedObjectRW extends SporkObjectBuilder[ReadWriter[PackedObject[_]]](macroRW)
  given packedObjectRW[T]: PackedSpork[ReadWriter[PackedObject[T]]] = PackedObjectRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedObject[T]]]]

  private[sporks] object PackedClassRW extends SporkObjectBuilder[ReadWriter[PackedClass[_]]](macroRW)
  given packedClassRW[T]: PackedSpork[ReadWriter[PackedClass[T]]] = PackedClassRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedClass[T]]]]

  private[sporks] object PackedLambdaRW extends SporkObjectBuilder[ReadWriter[PackedLambda[_]]](macroRW)
  given packedLambdaRW[T]: PackedSpork[ReadWriter[PackedLambda[T]]] = PackedLambdaRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedLambda[T]]]]

  private[sporks] object PackedEnvRW extends SporkObjectBuilder[ReadWriter[PackedEnv[_]]](macroRW)
  given packedEnvRW[E]: PackedSpork[ReadWriter[PackedEnv[E]]] = PackedEnvRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedEnv[E]]]]

  private[sporks] object PackedWithEnvRW extends SporkObjectBuilder[ReadWriter[PackedWithEnv[_, _]]](macroRW)
  given packedWithEnvRW[E, T]: PackedSpork[ReadWriter[PackedWithEnv[E, T]]] = PackedWithEnvRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedWithEnv[E, T]]]]

  private[sporks] object PackedWithCtxRW extends SporkObjectBuilder[ReadWriter[PackedWithCtx[_, _]]](macroRW)
  given packedWithCtxRW[E, T]: PackedSpork[ReadWriter[PackedWithCtx[E, T]]] = PackedWithCtxRW.pack().asInstanceOf[PackedSpork[ReadWriter[PackedWithCtx[E, T]]]]

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[F[T]]] for Option[T], List[T], etc.
  //////////////////////////////////////////////////////////////////////////////

  private[sporks] class SomeRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[Some[T]]]({ summon })
  given someRW[T](using tRW: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Some[T]]] = new SomeRW[T].pack().withCtx2(tRW)

  private[sporks] object NoneRW extends SporkObjectBuilder[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given noneRW: PackedSpork[ReadWriter[None.type]] = NoneRW.pack()

  private[sporks] class OptionRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[Option[T]]]({ summon })
  given optionRW[T](using tRW: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Option[T]]] = new OptionRW[T].pack().withCtx2(tRW)

  private[sporks] class ListRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[List[T]]]({ summon })
  given listRW[T](using tRW: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[List[T]]] = new ListRW[T].pack().withCtx2(tRW)

  private[sporks] object Tuple0RW extends SporkObjectBuilder[ReadWriter[EmptyTuple]](macroRW[EmptyTuple])
  given tuple0RW: PackedSpork[ReadWriter[EmptyTuple]] = Tuple0RW.pack()

  private[sporks] class Tuple1RW[T1] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[Tuple1[T1]]]({ summon })
  given tuple1RW[T1](using t1RW: PackedSpork[ReadWriter[T1]]): PackedSpork[ReadWriter[Tuple1[T1]]] = new Tuple1RW[T1].pack().withCtx2(t1RW)

  private[sporks] class Tuple2RW[T1, T2] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[Tuple2[T1, T2]]]({ summon })
  given tuple2RW[T1, T2](using t1RW: PackedSpork[ReadWriter[T1]], t2RW: PackedSpork[ReadWriter[T2]]): PackedSpork[ReadWriter[Tuple2[T1, T2]]] = (new Tuple2RW[T1, T2]).pack().withCtx2(t1RW).withCtx2(t2RW)

  private[sporks] class Tuple3RW[T1, T2, T3] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[T3] ?=> ReadWriter[Tuple3[T1, T2, T3]]]({ summon })
  given tuple3RW[T1, T2, T3](using t1RW: PackedSpork[ReadWriter[T1]], t2RW: PackedSpork[ReadWriter[T2]], t3RW: PackedSpork[ReadWriter[T3]]): PackedSpork[ReadWriter[Tuple3[T1, T2, T3]]] = (new Tuple3RW[T1, T2, T3]).pack().withCtx2(t1RW).withCtx2(t2RW).withCtx2(t3RW)

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[Spork[T]]
  //////////////////////////////////////////////////////////////////////////////

  given sporkRW2[T]: ReadWriter[Spork[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack())
  given sporkObjectRW2[T]: ReadWriter[SporkObject[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkObject[T]]]
  given sporkClassRW2[T]: ReadWriter[SporkClass[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkClass[T]]]
  given sporkLambdaRW2[T]: ReadWriter[SporkLambda[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkLambda[T]]]
  given sporkEnvRW2[T]: ReadWriter[SporkEnv[T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkEnv[T]]]
  given sporkWithEnvRW2[E, T]: ReadWriter[SporkWithEnv[E, T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkWithEnv[E, T]]]
  given sporkWithCtxRW2[E, T]: ReadWriter[SporkWithCtx[E, T]] = summon[ReadWriter[PackedSpork[T]]].bimap[Spork[T]](x => x.pack(), x => x.unpack()).asInstanceOf[ReadWriter[SporkWithCtx[E, T]]]

  //////////////////////////////////////////////////////////////////////////////
  // PackedSpork[ReadWriter[Spork[T]]]
  //////////////////////////////////////////////////////////////////////////////

  // Warning: Do not use `summon` instead of `sporkRW2` etc. here.

  private[sporks] object SporkRW extends SporkObjectBuilder[ReadWriter[Spork[_]]](sporkRW2.asInstanceOf)
  given sporkRW[T]: PackedSpork[ReadWriter[Spork[T]]] = SporkRW.pack().asInstanceOf[PackedSpork[ReadWriter[Spork[T]]]]

  private[sporks] object SporkObjectRW extends SporkObjectBuilder[ReadWriter[SporkObject[_]]](sporkObjectRW2.asInstanceOf)
  given sporkObjectRW[T]: PackedSpork[ReadWriter[SporkObject[T]]] = SporkObjectRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkObject[T]]]]

  private[sporks] object SporkClassRW extends SporkObjectBuilder[ReadWriter[SporkClass[_]]](sporkClassRW2.asInstanceOf)
  given sporkClassRW[T]: PackedSpork[ReadWriter[SporkClass[T]]] = SporkClassRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkClass[T]]]]

  private[sporks] object SporkLambdaRW extends SporkObjectBuilder[ReadWriter[SporkLambda[_]]](sporkLambdaRW2.asInstanceOf)
  given sporkLambdaRW[T]: PackedSpork[ReadWriter[SporkLambda[T]]] = SporkLambdaRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkLambda[T]]]]

  private[sporks] object SporkEnvRW extends SporkObjectBuilder[ReadWriter[SporkEnv[_]]](sporkEnvRW2.asInstanceOf)
  given sporkEnvRW[T]: PackedSpork[ReadWriter[SporkEnv[T]]] = SporkEnvRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkEnv[T]]]]

  private[sporks] object SporkWithEnvRW extends SporkObjectBuilder[ReadWriter[SporkWithEnv[_, _]]](sporkWithEnvRW2.asInstanceOf)
  given sporkWithEnvRW[E, T]: PackedSpork[ReadWriter[SporkWithEnv[E, T]]] = SporkWithEnvRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkWithEnv[E, T]]]]

  private[sporks] object SporkWithCtxRW extends SporkObjectBuilder[ReadWriter[SporkWithCtx[_, _]]](sporkWithCtxRW2.asInstanceOf)
  given sporkWithCtxRW[E, T]: PackedSpork[ReadWriter[SporkWithCtx[E, T]]] = SporkWithCtxRW.pack().asInstanceOf[PackedSpork[ReadWriter[SporkWithCtx[E, T]]]]

  //////////////////////////////////////////////////////////////////////////////
  // Spork[ReadWriter[T]] for T when PackedSpork[ReadWriter[T]] exists
  //////////////////////////////////////////////////////////////////////////////

  given sporkRWFromPacked[T](using PackedSpork[ReadWriter[T]]): Spork[ReadWriter[T]] = summon[PackedSpork[ReadWriter[T]]].unpack()

}
