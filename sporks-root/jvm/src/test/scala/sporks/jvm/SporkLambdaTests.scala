package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkLambdaTests:
  val lambda = Spork.apply[Int => Boolean] { x => x > 10 }

  val lambdaWithEnv = Spork.applyWithEnv(11) { x => x > 10 }

  object NestedLambda:
    val lambda = Spork.apply[Int => Boolean] { x => x > 10 }

  def methodLambda(): Spork[Int => Boolean] =
    Spork.apply[Int => Boolean] { x => x > 10 }

  def methodLambdaWithUnnusedArg(x: Int): Spork[Int => Boolean] =
    Spork.apply[Int => Boolean] { y => y > 10 }

  inline def inlinedMethodLambda(): Spork[Int => Boolean] =
    Spork.apply[Int => Boolean] { x => x > 10 }

  inline def inlinedMethodLambdaWithArg(x: Int): Spork[Int => Boolean] =
    Spork.apply[Int => Boolean] { y => y > x }

  class ClassWithLambda():
    val lambda = Spork.apply[Int => Boolean] { x => x > 10 }
    def methodLambda() = Spork.apply[Int => Boolean] { x => x > 10 }

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
    val packed9 = Spork.applyWithEnv(9) { x => x > 10 }
    val packed11 = Spork.applyWithEnv(11) { x => x > 10 }
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testLambdaWithCtx(): Unit =
    val packed9 = Spork.applyWithCtx(9) { summon[Int] > 10 }
    val packed11 = Spork.applyWithCtx(11) { summon[Int] > 10 }
    assertFalse(packed9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testPackBuildHigherOrderLambda(): Unit =
    val higherLevelFilter = Spork.apply[Spork[Int => Boolean] => Int => Option[Int]] { env => x => if env.unwrap().apply(x) then Some(x) else None }
    val filter = higherLevelFilter.withEnv(lambda).unwrap()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testPackedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"}"""

    val packed = upickle.default.write(lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testNestedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$NestedLambda$Lambda$12"}"""

    val packed = upickle.default.write(NestedLambda.lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Int => Boolean]](json).unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testPackedLambdaWithEnvReadWriter(): Unit =
    val json9 = """{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"9","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}}"""
    val json11 = """{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"11","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}}"""

    val packed9 = upickle.default.write(lambda.withEnv(9))
    val packed11 = upickle.default.write(lambda.withEnv(11))
    assertEquals(json9, packed9)
    assertEquals(json11, packed11)

    val loaded9 = upickle.default.read[Spork[Boolean]](json9).unwrap()
    val loaded11 = upickle.default.read[Spork[Boolean]](json11).unwrap()
    assertFalse(loaded9)
    assertTrue(loaded11)

  @Test
  def testLambdaWithEnvConstructorReadWriter(): Unit =
    val json = """{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$11"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"11","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}}"""

    val packed = upickle.default.write(lambdaWithEnv)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spork[Boolean]](json).unwrap()
    assertTrue(loaded)

  @Test
  def testLambdaWithOptionEnvironment(): Unit =
    val packed = Spork.applyWithEnv(Some(11)) { x => x.getOrElse(0) }
    val fun = packed.unwrap()
    assertEquals(11, fun)

  @Test
  def testLambdaWithListEnvironment(): Unit =
    val packed = Spork.applyWithEnv(List(1, 2, 3)) { x => x.sum }
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
