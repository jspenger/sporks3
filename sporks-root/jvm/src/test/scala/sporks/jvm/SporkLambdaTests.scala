package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkLambdaTests:
  val lambda = SporkBuilder.apply[Int => Boolean] { x => x > 10 }

  val lambdaWithEnv = SporkBuilder.applyWithEnv(11) { x => x > 10 }

  object NestedLambda:
    val lambda = SporkBuilder.apply[Int => Boolean] { x => x > 10 }

  def methodLambda(): PackedSpork[Int => Boolean] =
    SporkBuilder.apply[Int => Boolean] { x => x > 10 }

  def methodLambdaWithUnnusedArg(x: Int): PackedSpork[Int => Boolean] =
    SporkBuilder.apply[Int => Boolean] { y => y > 10 }

  inline def inlinedMethodLambda(): PackedSpork[Int => Boolean] =
    SporkBuilder.apply[Int => Boolean] { x => x > 10 }

  inline def inlinedMethodLambdaWithArg(x: Int): PackedSpork[Int => Boolean] =
    SporkBuilder.apply[Int => Boolean] { y => y > x }

  class ClassWithLambda():
    val lambda = SporkBuilder.apply[Int => Boolean] { x => x > 10 }
    def methodLambda() = SporkBuilder.apply[Int => Boolean] { x => x > 10 }

@RunWith(classOf[JUnit4])
class SporkLambdaTests:
  import SporkLambdaTests.*

  @Test
  def testLambda(): Unit =
    val predicate = lambda.unwrap()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testLambdaWithEnv(): Unit =
    val packed9 = SporkBuilder.applyWithEnv(9) { x => x > 10 }
    val packed11 = SporkBuilder.applyWithEnv(11) { x => x > 10 }
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testLambdaWithCtx(): Unit =
    val packed9 = SporkBuilder.applyWithCtx(9) { summon[Int] > 10 }
    val packed11 = SporkBuilder.applyWithCtx(11) { summon[Int] > 10 }
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testPackBuildHigherOrderLambda(): Unit =
    val higherLevelFilter = SporkBuilder.apply[PackedSpork[Int => Boolean] => Int => Option[Int]] { env => x => if env.unwrap().apply(x) then Some(x) else None }
    val filter = higherLevelFilter.packWithEnv(lambda).unwrap()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testPackedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$11"}"""

    val packed = upickle.default.write(lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testNestedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$NestedLambda$Lambda$13"}"""

    val packed = upickle.default.write(NestedLambda.lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testPackedLambdaWithEnvReadWriter(): Unit =
    val json9 = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$11"},"packedEnv":{"$type":"sporks.PackedEnv","env":"9","rw":{"$type":"sporks.PackedObject","fun":"sporks.ReadWriters$INT_RW$"}}}"""
    val json11 = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$11"},"packedEnv":{"$type":"sporks.PackedEnv","env":"11","rw":{"$type":"sporks.PackedObject","fun":"sporks.ReadWriters$INT_RW$"}}}"""

    val packed9 = upickle.default.write(lambda.packWithEnv(9))
    val packed11 = upickle.default.write(lambda.packWithEnv(11))
    assertEquals(json9, packed9)
    assertEquals(json11, packed11)

    val loaded9 = upickle.default.read[PackedSpork[Boolean]](json9).unwrap()
    val loaded11 = upickle.default.read[PackedSpork[Boolean]](json11).unwrap()
    assertFalse(loaded9)
    assertTrue(loaded11)

  @Test
  def testLambdaWithEnvConstructorReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$12"},"packedEnv":{"$type":"sporks.PackedEnv","env":"11","rw":{"$type":"sporks.PackedObject","fun":"sporks.ReadWriters$INT_RW$"}}}"""

    val packed = upickle.default.write(lambdaWithEnv)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Boolean]](json).unwrap()
    assertTrue(loaded)

  @Test
  def testLambdaWithOptionEnvironment(): Unit =
    val packed = SporkBuilder.applyWithEnv(Some(11)) { x => x.getOrElse(0) }
    val fun = packed.unwrap()
    assertEquals(11, fun)

  @Test
  def testLambdaWithListEnvironment(): Unit =
    val packed = SporkBuilder.applyWithEnv(List(1, 2, 3)) { x => x.sum }
    val fun = packed.unwrap()
    assertEquals(6, fun)

  @Test
  def testLambdaFromMethodCreator(): Unit =
    val packed = methodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromMethodCreatorWithUnnusedArg(): Unit =
    val packed = methodLambdaWithUnnusedArg(11)
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreator(): Unit =
    val packed = inlinedMethodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreatorWithArg(): Unit =
    val packed = inlinedMethodLambdaWithArg(10)
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassCreator(): Unit =
    val packed = ClassWithLambda().lambda
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassMethodCreator(): Unit =
    val packed = ClassWithLambda().methodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testUnpackUnwrap(): Unit =
    val packed = lambda
    val unpacked = packed.unpack()
    val unwrapped = unpacked.unwrap()
    assertTrue(unwrapped(11))
    assertFalse(unwrapped(9))

  @Test
  def testUnpackUnwrapWithEnv(): Unit =
    val packed = lambdaWithEnv
    val unpacked = packed.unpack()
    val unwrapped = unpacked.unwrap()
    assertTrue(unwrapped)

  @Test
  def testUnpackUnwrapWithCtx(): Unit =
    val packed = SporkBuilder.applyWithCtx(11) { summon[Int] > 10 }
    val unpacked = packed.unpack()
    val unwrapped = unpacked.unwrap()
    assertTrue(unwrapped)
