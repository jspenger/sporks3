package object sporks {

  import upickle.default.*
  import scala.util.*

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
  // An implementation with `SporkClass` would look like this:
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

  private abstract class UnpackingCombinator[T, U](fun: T ?=> U) extends SporkClass[PackedSpork[T] ?=> U]({ packed ?=> fun.apply(using packed.build()) })

  class SOME_RW[T] extends UnpackingCombinator[ReadWriter[T], ReadWriter[Some[T]]]({ summon[ReadWriter[Some[T]]] })
  given some_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Some[T]]] = new SOME_RW[T].pack().packWithCtx(t_rw)

  object NONE_RW extends SporkObject[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given none_rw: PackedSpork[ReadWriter[None.type]] = NONE_RW.pack()

  class OPTION_RW[T] extends UnpackingCombinator[ReadWriter[T], ReadWriter[Option[T]]]({ summon[ReadWriter[Option[T]]] })
  given option_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[Option[T]]] = (new OPTION_RW[T]).pack().packWithCtx(t_rw)

  class LIST_RW[T] extends UnpackingCombinator[ReadWriter[T], ReadWriter[List[T]]]({ summon[ReadWriter[List[T]]] })
  given list_rw[T](using t_rw: PackedSpork[ReadWriter[T]]): PackedSpork[ReadWriter[List[T]]] = new LIST_RW[T].pack().packWithCtx(t_rw)

}
