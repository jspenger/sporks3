package sporks

import upickle.default.*

import sporks.*
import sporks.Packed.*


/** A collection of ReadWriters. Contains both `ReadWriter[Spork[T]]` and
  * `Spork[ReadWriter[T]]` for various `T`.
  *
  * Use `ReadWriter[Spork[T]]` to serialize and deserialize Sporks. For example,
  * by using the `upickle.default.write` and `upickle.default.read` methods
  * applied to a `Spork[T]`.
  *
  * Use `Spork[ReadWriter[T]]` when packing a value of type `T` into a Spork.
  * For example, by using the `withEnv` and `withCtx` methods of a `Spork[T =>
  * R]` or `Spork[T ?=> R]`.
  */
object ReadWriters {

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[Spork[T]]
  //////////////////////////////////////////////////////////////////////////////

  given [T]: ReadWriter[Spork[T]]  = macroRW
  given [T]: ReadWriter[PackedObject[T]] = macroRW
  given [T]: ReadWriter[PackedClass[T]]  = macroRW
  given [T]: ReadWriter[PackedLambda[T]] = macroRW
  given [E]: ReadWriter[PackedEnv[E]]    = macroRW
  given [E, T]: ReadWriter[PackedWithEnv[E, T]] = macroRW
  given [E, T]: ReadWriter[PackedWithCtx[E, T]] = macroRW

  //////////////////////////////////////////////////////////////////////////////
  // Spork[ReadWriter[T]] for primitive T
  //////////////////////////////////////////////////////////////////////////////

  private[sporks] object IntRW extends SporkBuilder[ReadWriter[Int]](summon[ReadWriter[Int]])
  given intRW: Spork[ReadWriter[Int]] = IntRW.pack()

  private[sporks] object StringRW extends SporkBuilder[ReadWriter[String]](summon[ReadWriter[String]])
  given strRW: Spork[ReadWriter[String]] = StringRW.pack()

  private[sporks] object BooleanRW extends SporkBuilder[ReadWriter[Boolean]](summon[ReadWriter[Boolean]])
  given boolRW: Spork[ReadWriter[Boolean]] = BooleanRW.pack()

  private[sporks] object DoubleRW extends SporkBuilder[ReadWriter[Double]](summon[ReadWriter[Double]])
  given doubleRW: Spork[ReadWriter[Double]] = DoubleRW.pack()

  private[sporks] object FloatRW extends SporkBuilder[ReadWriter[Float]](summon[ReadWriter[Float]])
  given floatRW: Spork[ReadWriter[Float]] = FloatRW.pack()

  private[sporks] object LongRW extends SporkBuilder[ReadWriter[Long]](summon[ReadWriter[Long]])
  given longRW: Spork[ReadWriter[Long]] = LongRW.pack()

  private[sporks] object ShortRW extends SporkBuilder[ReadWriter[Short]](summon[ReadWriter[Short]])
  given shortRW: Spork[ReadWriter[Short]] = ShortRW.pack()

  private[sporks] object ByteRW extends SporkBuilder[ReadWriter[Byte]](summon[ReadWriter[Byte]])
  given byteRW: Spork[ReadWriter[Byte]] = ByteRW.pack()

  private[sporks] object CharRW extends SporkBuilder[ReadWriter[Char]](summon[ReadWriter[Char]])
  given charRW: Spork[ReadWriter[Char]] = CharRW.pack()

  private[sporks] object UnitRW extends SporkBuilder[ReadWriter[Unit]](summon[ReadWriter[Unit]])
  given unitRW: Spork[ReadWriter[Unit]] = UnitRW.pack()

  //////////////////////////////////////////////////////////////////////////////
  // Spork[ReadWriter[Spork[?]]]
  //////////////////////////////////////////////////////////////////////////////

  private[sporks] object SporkRW extends SporkBuilder[ReadWriter[Spork[?]]](macroRW)
  given packedSporkRW[T]: Spork[ReadWriter[Spork[T]]] = SporkRW.pack().asInstanceOf[Spork[ReadWriter[Spork[T]]]]

  private[sporks] object PackedObjectRW extends SporkBuilder[ReadWriter[PackedObject[?]]](macroRW)
  given packedObjectRW[T]: Spork[ReadWriter[PackedObject[T]]] = PackedObjectRW.pack().asInstanceOf[Spork[ReadWriter[PackedObject[T]]]]

  private[sporks] object PackedClassRW extends SporkBuilder[ReadWriter[PackedClass[?]]](macroRW)
  given packedClassRW[T]: Spork[ReadWriter[PackedClass[T]]] = PackedClassRW.pack().asInstanceOf[Spork[ReadWriter[PackedClass[T]]]]

  private[sporks] object PackedLambdaRW extends SporkBuilder[ReadWriter[PackedLambda[?]]](macroRW)
  given packedLambdaRW[T]: Spork[ReadWriter[PackedLambda[T]]] = PackedLambdaRW.pack().asInstanceOf[Spork[ReadWriter[PackedLambda[T]]]]

  private[sporks] object PackedEnvRW extends SporkBuilder[ReadWriter[PackedEnv[?]]](macroRW)
  given packedEnvRW[E]: Spork[ReadWriter[PackedEnv[E]]] = PackedEnvRW.pack().asInstanceOf[Spork[ReadWriter[PackedEnv[E]]]]

  private[sporks] object PackedWithEnvRW extends SporkBuilder[ReadWriter[PackedWithEnv[?, ?]]](macroRW)
  given packedWithEnvRW[E, T]: Spork[ReadWriter[PackedWithEnv[E, T]]] = PackedWithEnvRW.pack().asInstanceOf[Spork[ReadWriter[PackedWithEnv[E, T]]]]

  private[sporks] object PackedWithCtxRW extends SporkBuilder[ReadWriter[PackedWithCtx[?, ?]]](macroRW)
  given packedWithCtxRW[E, T]: Spork[ReadWriter[PackedWithCtx[E, T]]] = PackedWithCtxRW.pack().asInstanceOf[Spork[ReadWriter[PackedWithCtx[E, T]]]]

  //////////////////////////////////////////////////////////////////////////////
  // Spork[ReadWriter[F[T]]] for Option[T], List[T], etc.
  //////////////////////////////////////////////////////////////////////////////

  private[sporks] class SomeRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[Some[T]]]({ summon })
  given someRW[T](using tRW: Spork[ReadWriter[T]]): Spork[ReadWriter[Some[T]]] = new SomeRW[T].pack().withCtx2(tRW)

  private[sporks] object NoneRW extends SporkBuilder[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given noneRW: Spork[ReadWriter[None.type]] = NoneRW.pack()

  private[sporks] class OptionRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[Option[T]]]({ summon })
  given optionRW[T](using tRW: Spork[ReadWriter[T]]): Spork[ReadWriter[Option[T]]] = new OptionRW[T].pack().withCtx2(tRW)

  private[sporks] class ListRW[T] extends SporkClassBuilder[ReadWriter[T] ?=> ReadWriter[List[T]]]({ summon })
  given listRW[T](using tRW: Spork[ReadWriter[T]]): Spork[ReadWriter[List[T]]] = new ListRW[T].pack().withCtx2(tRW)

  private[sporks] object Tuple0RW extends SporkBuilder[ReadWriter[EmptyTuple]](macroRW[EmptyTuple])
  given tuple0RW: Spork[ReadWriter[EmptyTuple]] = Tuple0RW.pack()

  private[sporks] class Tuple1RW[T1] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[Tuple1[T1]]]({ summon })
  given tuple1RW[T1](using t1RW: Spork[ReadWriter[T1]]): Spork[ReadWriter[Tuple1[T1]]] = new Tuple1RW[T1].pack().withCtx2(t1RW)

  private[sporks] class Tuple2RW[T1, T2] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[Tuple2[T1, T2]]]({ summon })
  given tuple2RW[T1, T2](using t1RW: Spork[ReadWriter[T1]], t2RW: Spork[ReadWriter[T2]]): Spork[ReadWriter[Tuple2[T1, T2]]] = (new Tuple2RW[T1, T2]).pack().withCtx2(t1RW).withCtx2(t2RW)

  private[sporks] class Tuple3RW[T1, T2, T3] extends SporkClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[T3] ?=> ReadWriter[Tuple3[T1, T2, T3]]]({ summon })
  given tuple3RW[T1, T2, T3](using t1RW: Spork[ReadWriter[T1]], t2RW: Spork[ReadWriter[T2]], t3RW: Spork[ReadWriter[T3]]): Spork[ReadWriter[Tuple3[T1, T2, T3]]] = (new Tuple3RW[T1, T2, T3]).pack().withCtx2(t1RW).withCtx2(t2RW).withCtx2(t3RW)

}
