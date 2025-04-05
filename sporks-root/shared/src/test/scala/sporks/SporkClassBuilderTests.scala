package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkClassBuilderTests:
  class Thunk[T] extends SporkClassBuilder[T => () => T](t => () => t)

  class Predicate extends SporkClassBuilder[Int => Boolean](x => x > 10)

  class FilterWithTypeParam[T] extends SporkClassBuilder[PackedSpork[T => Boolean] => T => Option[T]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  class Flatten[T] extends SporkClassBuilder[List[List[T]] => List[T]](x => x.flatten)

  object NestedBuilder:
    class Predicate extends SporkClassBuilder[Int => Boolean](x => x > 10)

@RunWith(classOf[JUnit4])
class SporkClassBuilderTests:
  import SporkClassBuilderTests.*

  @Test
  def testSporkClassBuilderPack(): Unit =
    val packed = new Predicate().pack()
    val predicate = packed.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

    val built = new Predicate().build()
    val predicate2 = built.unwrap()
    assertTrue(predicate2(11))
    assertFalse(predicate2(9))

  @Test
  def testSporkClassBuilderWithEnv(): Unit =
    val packed = new Thunk[Int].pack().packWithEnv(10)
    val thunk = packed.unwrap()
    assertEquals(10, thunk())

    val built = new Thunk[Int].build().withEnv(10)
    val thunk2 = built.unwrap()
    assertEquals(10, thunk2())

  @Test
  def testSporkClassBuilderWithTypeParam(): Unit =
    val packed = new Flatten[Int].pack()
    val flatten = packed.unwrap()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, flatten(nestedList))

    val built = new Flatten[Int].build()
    val flatten2 = built.unwrap()
    assertEquals(nestedList.flatten, flatten2(nestedList))


  @Test
  def testHigherLevelSporkClassBuilder(): Unit =
    val packed = new FilterWithTypeParam[Int].pack()
    val filter = packed.unwrap()
    val predicate = new Predicate().pack()
    assertEquals(Some(11), filter(predicate)(11))
    assertEquals(None, filter(predicate)(9))

    val built = new FilterWithTypeParam[Int].build()
    val filter2 = built.unwrap()
    assertEquals(Some(11), filter2(predicate)(11))
    assertEquals(None, filter2(predicate)(9))

  @Test
  def testPackedSporkClassBuilderReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedClass","fun":"sporks.SporkClassBuilderTests$Predicate"}"""

    val packed = upickle.default.write(new Predicate().pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

    val packed2 = upickle.default.write(new Predicate().build())
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded2(11))
    assertFalse(loaded2(9))

  @Test
  def testPackedSporkClassBuilderWithTypeParamReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedClass","fun":"sporks.SporkClassBuilderTests$Flatten"}"""

    val packed = upickle.default.write(new Flatten[Int].pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[List[List[Int]] => List[Int]]](json).unwrap()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, loaded(nestedList))

    val packed2 = upickle.default.write(new Flatten[Int].build())
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[List[List[Int]] => List[Int]]](json).unwrap()
    assertEquals(nestedList.flatten, loaded2(nestedList))

  @Test
  def testPackedSporkClassBuilderWithEnv(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedWithEnv","packed":{"$type":"sporks.PackedSpork.PackedClass","fun":"sporks.SporkClassBuilderTests$FilterWithTypeParam"},"packedEnv":{"$type":"sporks.PackedSpork.PackedEnv","env":"{\"$type\":\"sporks.PackedSpork.PackedClass\",\"fun\":\"sporks.SporkClassBuilderTests$Predicate\"}","rw":{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.ReadWriters$PackedClassRW$"}}}"""

    val predicate = new Predicate().pack()
    val filter = new FilterWithTypeParam[Int].pack().packWithEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))

    val packed2 = upickle.default.write(new FilterWithTypeParam[Int].build().withEnv(predicate))
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded2(11))
    assertEquals(None, loaded2(9))
