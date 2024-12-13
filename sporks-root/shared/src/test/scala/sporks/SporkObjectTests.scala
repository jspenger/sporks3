package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkObjectTests:
  object Thunk extends SporkObject[() => Int](() => 10)

  object Predicate extends SporkObject[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporkObject[PackedSpork[Int => Boolean] => Int => Option[Int]]({ env => x => if env.build().apply(x) then Some(x) else None })

  object PredicateCtx extends SporkObject[Int ?=> Boolean](summon[Int] > 10)

  object OptionMapper extends SporkObject[Option[Int] => Int](x => x.getOrElse(0))

  object ListReducer extends SporkObject[List[Int] => Int](x => x.sum)

  object NestedBuilder:
    object Predicate extends SporkObject[Int => Boolean](x => x > 10)

  object ShouldError:
    class NotObjObj extends SporkObject[Int => Int](x => x)

    class SomeClass:
      object NotTopLevel extends SporkObject[Int => Int](x => x)

    def someMethod: SporkObject[Int => Int] = {
      object NotTopLevel extends SporkObject[Int => Int](x => x)
      NotTopLevel
    }

@RunWith(classOf[JUnit4])
class SporkObjectTests:
  import SporkObjectTests.*

  @Test
  def testSporkObjectPack(): Unit =
    val packed = Predicate.pack()
    val predicate = packed.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testNestedSporkObjectPack(): Unit =
    val packed = NestedBuilder.Predicate.pack()
    val predicate = packed.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testSporkObjectThunk(): Unit =
    val packed = Thunk.pack()
    val thunk = packed.build()
    assertEquals(10, thunk())

  @Test
  def testPackWithEnv(): Unit =
    val packed9 = Predicate.pack().packWithEnv(9)
    val packed11 = Predicate.pack().packWithEnv(11)
    assertFalse(packed9.build())
    assertTrue(packed11.build())

  @Test
  def testPackWithCtx(): Unit =
    val packed9 = PredicateCtx.pack().packWithCtx(9)
    val packed11 = PredicateCtx.pack().packWithCtx(11)
    assertFalse(packed9.build())
    assertTrue(packed11.build())

  @Test
  def testPackBuildHigherOrderSporkObject(): Unit =
    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().packWithEnv(predicate).build()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testPackedSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedObject","fun":"sporks.SporkObjectTests$Predicate$"}"""

    val packed = upickle.default.write(Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).build()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testNestedPackedSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedObject","fun":"sporks.SporkObjectTests$NestedBuilder$Predicate$"}"""

    val packed = upickle.default.write(NestedBuilder.Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).build()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testPackedSporkReadWriterWithEnv(): Unit =
    val json = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedObject","fun":"sporks.SporkObjectTests$HigherLevelFilter$"},"env":"{\"$type\":\"sporks.PackedObject\",\"fun\":\"sporks.SporkObjectTests$Predicate$\"}","envRW":{"$type":"sporks.PackedObject","fun":"sporks.package$PACKED_OBJECT_RW$"}}"""

    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().packWithEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded =
      upickle.default.read[PackedSpork[Int => Option[Int]]](json).build()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))

  @Test
  def testOptionEnvironment(): Unit =
    val packed = OptionMapper.pack().packWithEnv(Some(11))
    val fun = packed.build()
    assertEquals(11, fun)

  @Test
  def testListEnvironment(): Unit =
    val packed = ListReducer.pack().packWithEnv(List(1, 2, 3))
    val fun = packed.build()
    assertEquals(6, fun)

  @Test
  def testClassSporkObjectError(): Unit =
    // The provided SporkObject `new sporks.SporkObjectTests.ShouldError.NotObjObj()` is not an object.
    assertTrue:
      typeCheckErrors:
        """
        new ShouldError.NotObjObj().pack()
        """
      .contains:
        """
        The provided SporkObject `new sporks.SporkObjectTests.ShouldError.NotObjObj()` is not an object.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notObjObj = new ShouldError.NotObjObj()
        notObjObj.pack()
        """
      .contains:
        """
        The provided SporkObject `notObjObj` is not an object.
        """.strip()

  @Test
  def testNotTopLevelError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        val notTopLevel = new ShouldError.SomeClass().NotTopLevel
        notTopLevel.pack()
        """
      .contains:
        """
        The provided SporkObject `notTopLevel` is not a top-level object; its owner `SomeClass` is not a top-level object nor a package.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notObject = ShouldError.someMethod
        notObject.pack()
        """
      .contains:
        """
        The provided SporkObject `notObject` is not an object.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        object Builder extends SporkObject[Int => String](x => x.toString.reverse)
        Builder.pack()
        """
      .contains:
        """
        The provided SporkObject `Builder` is not a top-level object; its owner `testNotTopLevelError` is not a top-level object nor a package.
        """.strip()
