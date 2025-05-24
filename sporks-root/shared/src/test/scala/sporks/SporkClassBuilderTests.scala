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

  class FilterWithTypeParam[T] extends SporkClassBuilder[Spork[T => Boolean] => T => Option[T]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

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

  @Test
  def testSporkClassBuilderWithEnv(): Unit =
    val packed = new Thunk[Int].pack().withEnv(10)
    val thunk = packed.unwrap()
    assertEquals(10, thunk())

  @Test
  def testSporkClassBuilderWithTypeParam(): Unit =
    val packed = new Flatten[Int].pack()
    val flatten = packed.unwrap()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, flatten(nestedList))

  @Test
  def testHigherLevelSporkClassBuilder(): Unit =
    val packed = new FilterWithTypeParam[Int].pack()
    val filter = packed.unwrap()
    val predicate = new Predicate().pack()
    assertEquals(Some(11), filter(predicate)(11))
    assertEquals(None, filter(predicate)(9))

  @Test
  def testSporkClassBuilderReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedClass","fun":"sporks.SporkClassBuilderTests$Predicate"}"""

    val packed = upickle.default.write(new Predicate().pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testSporkClassBuilderWithTypeParamReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedClass","fun":"sporks.SporkClassBuilderTests$Flatten"}"""

    val packed = upickle.default.write(new Flatten[Int].pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[List[List[Int]] => List[Int]]](json).unwrap()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, loaded(nestedList))

  @Test
  def testSporkClassBuilderWithEnvReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedClass","fun":"sporks.SporkClassBuilderTests$FilterWithTypeParam"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"{\"$type\":\"sporks.Packed.PackedClass\",\"fun\":\"sporks.SporkClassBuilderTests$Predicate\"}","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$SporkRW$"}}}"""

    val predicate = new Predicate().pack()
    val filter = new FilterWithTypeParam[Int].pack().withEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))
