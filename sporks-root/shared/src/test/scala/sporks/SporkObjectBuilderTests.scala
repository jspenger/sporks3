package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkObjectBuilderTests:
  object Thunk extends SporkObjectBuilder[() => Int](() => 10)

  object Predicate extends SporkObjectBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporkObjectBuilder[PackedSpork[Int => Boolean] => Int => Option[Int]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  object PredicateCtx extends SporkObjectBuilder[Int ?=> Boolean](summon[Int] > 10)

  object OptionMapper extends SporkObjectBuilder[Option[Int] => Int](x => x.getOrElse(0))

  object ListReducer extends SporkObjectBuilder[List[Int] => Int](x => x.sum)

  object NestedBuilder:
    object Predicate extends SporkObjectBuilder[Int => Boolean](x => x > 10)

@RunWith(classOf[JUnit4])
class SporkObjectBuilderTests:
  import SporkObjectBuilderTests.*

  @Test
  def testSporkObjectBuilderPack(): Unit =
    val packed = Predicate.pack()
    val predicate = packed.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

    val built = Predicate.build()
    val predicate2 = built.unwrap()
    assertTrue(predicate2(11))
    assertFalse(predicate2(9))

  @Test
  def testNestedSporkObjectBuilderPack(): Unit =
    val packed = NestedBuilder.Predicate.pack()
    val predicate = packed.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

    val built = NestedBuilder.Predicate.build()
    val predicate2 = built.unwrap()
    assertTrue(predicate2(11))
    assertFalse(predicate2(9))

  @Test
  def testSporkObjectBuilderThunk(): Unit =
    val packed = Thunk.pack()
    val thunk = packed.unwrap()
    assertEquals(10, thunk())

    val built = Thunk.build()
    val thunk2 = built.unwrap()
    assertEquals(10, thunk2())

  @Test
  def testPackWithEnv(): Unit =
    val packed9 = Predicate.pack().packWithEnv(9)
    val packed11 = Predicate.pack().packWithEnv(11)
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

    val built9 = Predicate.build().withEnv(9)
    val built11 = Predicate.build().withEnv(11)
    assertFalse(built9.unwrap())
    assertTrue(built11.unwrap())

  @Test
  def testPackWithCtx(): Unit =
    val packed9 = PredicateCtx.pack().packWithCtx(9)
    val packed11 = PredicateCtx.pack().packWithCtx(11)
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

    val built9 = PredicateCtx.build().withCtx(9)
    val built11 = PredicateCtx.build().withCtx(11)
    assertFalse(built9.unwrap())
    assertTrue(built11.unwrap())

  @Test
  def testPackBuildHigherOrderSporkObjectBuilder(): Unit =
    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().packWithEnv(predicate).unwrap()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

    val predicate2 = Predicate.build()
    val filter2 = HigherLevelFilter.build().withEnv(predicate).unwrap()
    assertEquals(Some(11), filter2(11))
    assertEquals(None, filter2(9))

  @Test
  def testPackedSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.SporkObjectBuilderTests$Predicate$"}"""

    val packed = upickle.default.write(Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

    val packed2 = upickle.default.write(Predicate.build())
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded2(11))
    assertFalse(loaded2(9))

  @Test
  def testNestedPackedSporkReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.SporkObjectBuilderTests$NestedBuilder$Predicate$"}"""

    val packed = upickle.default.write(NestedBuilder.Predicate.pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

    val packed2 = upickle.default.write(NestedBuilder.Predicate.build())
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded2(11))
    assertFalse(loaded2(9))

  @Test
  def testPackedSporkReadWriterWithEnv(): Unit =
    val json = """{"$type":"sporks.PackedSpork.PackedWithEnv","packed":{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.SporkObjectBuilderTests$HigherLevelFilter$"},"packedEnv":{"$type":"sporks.PackedSpork.PackedEnv","env":"{\"$type\":\"sporks.PackedSpork.PackedObject\",\"fun\":\"sporks.SporkObjectBuilderTests$Predicate$\"}","rw":{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.ReadWriters$PackedObjectRW$"}}}"""

    val predicate = Predicate.pack()
    val filter = HigherLevelFilter.pack().packWithEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))

    val packed2 = upickle.default.write(HigherLevelFilter.build().withEnv(predicate))
    assertEquals(json, packed2)

    val loaded2 = upickle.default.read[Spork[Int => Option[Int]]](json).unwrap()
    assertEquals(Some(11), loaded2(11))
    assertEquals(None, loaded2(9))

  @Test
  def testOptionEnvironment(): Unit =
    val packed = OptionMapper.pack().packWithEnv(Some(11))
    val fun = packed.unwrap()
    assertEquals(11, fun)

    val packed2 = OptionMapper.pack().packWithEnv(Some(11))
    val fun2 = packed2.unwrap()
    assertEquals(11, fun2)

  @Test
  def testListEnvironment(): Unit =
    val packed = ListReducer.pack().packWithEnv(List(1, 2, 3))
    val fun = packed.unwrap()
    assertEquals(6, fun)

    val packed2 = ListReducer.pack().packWithEnv(List(1, 2, 3))
    val fun2 = packed2.unwrap()
    assertEquals(6, fun2)
