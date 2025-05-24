package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkBuilderTests:
  object Thunk extends SporkBuilder[() => Int](() => 10)

  object Predicate extends SporkBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporkBuilder[Spork[Int => Boolean] => Int => Option[Int]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  object PredicateCtx extends SporkBuilder[Int ?=> Boolean](summon[Int] > 10)

  object OptionMapper extends SporkBuilder[Option[Int] => Int](x => x.getOrElse(0))

  object ListReducer extends SporkBuilder[List[Int] => Int](x => x.sum)

  object NestedBuilder:
    object Predicate extends SporkBuilder[Int => Boolean](x => x > 10)

@RunWith(classOf[JUnit4])
class SporkBuilderTests:
  import SporkBuilderTests.*

  @Test
  def testSporkBuilderPack(): Unit =
    val packed = Predicate.pack()
    val predicate = packed.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testNestedSporkBuilderPack(): Unit =
    val packed = NestedBuilder.Predicate.pack()
    val predicate = packed.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testSporkBuilderThunk(): Unit =
    val packed = Thunk.pack()
    val thunk = packed.unwrap()
    assertEquals(10, thunk())

  @Test
  def testWithEnv(): Unit =
    val packed9 = Predicate.pack().withEnv(9)
    val packed11 = Predicate.pack().withEnv(11)
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testWithCtx(): Unit =
    val packed9 = PredicateCtx.pack().withCtx(9)
    val packed11 = PredicateCtx.pack().withCtx(11)
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testPackBuildHigherOrderSporkBuilder(): Unit =
    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().withEnv(predicate).unwrap()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedObject","fun":"sporks.SporkBuilderTests$Predicate$"}"""

    val packed = upickle.default.write(Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testNestedSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedObject","fun":"sporks.SporkBuilderTests$NestedBuilder$Predicate$"}"""

    val packed = upickle.default.write(NestedBuilder.Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testSporkReadWriterWithEnv(): Unit =
    val json = """{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedObject","fun":"sporks.SporkBuilderTests$HigherLevelFilter$"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"{\"$type\":\"sporks.Packed.PackedObject\",\"fun\":\"sporks.SporkBuilderTests$Predicate$\"}","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$SporkRW$"}}}"""

    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().withEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))

  @Test
  def testOptionEnvironment(): Unit =
    val packed = OptionMapper.pack().withEnv(Some(11))
    val fun = packed.unwrap()
    assertEquals(11, fun)

    val packed2 = OptionMapper.pack().withEnv(Some(11))
    val fun2 = packed2.unwrap()
    assertEquals(11, fun2)

  @Test
  def testListEnvironment(): Unit =
    val packed = ListReducer.pack().withEnv(List(1, 2, 3))
    val fun = packed.unwrap()
    assertEquals(6, fun)

    val packed2 = ListReducer.pack().withEnv(List(1, 2, 3))
    val fun2 = packed2.unwrap()
    assertEquals(6, fun2)
