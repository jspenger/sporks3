package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.jvm.*
import sporks.TestUtils.*

object SporkLambdaTests:
  val lambda = Spork.apply[Int => Boolean] { x => x > 10 }

  val lambdaWithEnv = Spork.applyWithEnv(11) { x => x > 10 }

  object NestedLambda:
    val lambda = Spork.apply[Int => Boolean] { x => x > 10 }

  def methodLambda(): PackedSpork[Int => Boolean] =
    Spork.apply[Int => Boolean] { x => x > 10 }

  def methodLambdaWithUnnusedArg(x: Int): PackedSpork[Int => Boolean] =
    Spork.apply[Int => Boolean] { y => y > 10 }

  inline def inlinedMethodLambda(): PackedSpork[Int => Boolean] =
    Spork.apply[Int => Boolean] { x => x > 10 }

  inline def inlinedMethodLambdaWithArg(x: Int): PackedSpork[Int => Boolean] =
    Spork.apply[Int => Boolean] { y => y > x }

  class ClassWithLambda():
    val lambda = Spork.apply[Int => Boolean] { x => x > 10 }
    def methodLambda() = Spork.apply[Int => Boolean] { x => x > 10 }

@RunWith(classOf[JUnit4])
class SporkLambdaTests:
  import SporkLambdaTests.*

  @Test
  def testLambda(): Unit =
    val predicate = lambda.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testLambdaWithEnv(): Unit =
    val packed9 = Spork.applyWithEnv(9) { x => x > 10 }
    val packed11 = Spork.applyWithEnv(11) { x => x > 10 }
    assertFalse(packed9.build())
    assertTrue(packed11.build())

  @Test
  def testLambdaWithCtx(): Unit =
    val packed9 = Spork.applyWithCtx(9) { summon[Int] > 10 }
    val packed11 = Spork.applyWithCtx(11) { summon[Int] > 10 }
    assertFalse(packed9.build())
    assertTrue(packed11.build())

  @Test
  def testPackBuildHigherOrderLambda(): Unit =
    val higherLevelFilter = Spork.apply[PackedSpork[Int => Boolean] => Int => Option[Int]] { env => x => if env.build().apply(x) then Some(x) else None }
    val filter = higherLevelFilter.packWithEnv(lambda).build()
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testPackedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"}"""

    val packed = upickle.default.write(lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).build()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testNestedLambdaReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$NestedLambda$Lambda$12"}"""

    val packed = upickle.default.write(NestedLambda.lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).build()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testPackedLambdaWithEnvReadWriter(): Unit =
    val json9 = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"},"env":"9","envRW":{"$type":"sporks.PackedObject","fun":"sporks.package$INT_RW$"}}"""
    val json11 = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$10"},"env":"11","envRW":{"$type":"sporks.PackedObject","fun":"sporks.package$INT_RW$"}}"""

    val packed9 = upickle.default.write(lambda.packWithEnv(9))
    val packed11 = upickle.default.write(lambda.packWithEnv(11))
    assertEquals(json9, packed9)
    assertEquals(json11, packed11)

    val loaded9 = upickle.default.read[PackedSpork[Boolean]](json9).build()
    val loaded11 = upickle.default.read[PackedSpork[Boolean]](json11).build()
    assertFalse(loaded9)
    assertTrue(loaded11)

  @Test
  def testLambdaWithEnvConstructorReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedLambda","fun":"sporks.jvm.SporkLambdaTests$Lambda$11"},"env":"11","envRW":{"$type":"sporks.PackedObject","fun":"sporks.package$INT_RW$"}}"""

    val packed = upickle.default.write(lambdaWithEnv)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Boolean]](json).build()
    assertTrue(loaded)

  @Test
  def testLambdaWithOptionEnvironment(): Unit =
    val packed = Spork.applyWithEnv(Some(11)) { x => x.getOrElse(0) }
    val fun = packed.build()
    assertEquals(11, fun)

  @Test
  def testLambdaWithListEnvironment(): Unit =
    val packed = Spork.applyWithEnv(List(1, 2, 3)) { x => x.sum }
    val fun = packed.build()
    assertEquals(6, fun)

  @Test
  def testLambdaFromMethodCreator(): Unit =
    val packed = methodLambda()
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromMethodCreatorWithUnnusedArg(): Unit =
    val packed = methodLambdaWithUnnusedArg(11)
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreator(): Unit =
    val packed = inlinedMethodLambda()
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreatorWithArg(): Unit =
    val packed = inlinedMethodLambdaWithArg(10)
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassCreator(): Unit =
    val packed = ClassWithLambda().lambda
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassMethodCreator(): Unit =
    val packed = ClassWithLambda().methodLambda()
    val fun = packed.build()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testInvalidCaptureIdent(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        val y = 12
        Spork.apply[Int => Int] { x => x + y }
        """
      .contains:
        """
        Invalid capture of variable `y`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        Spork.apply[Int => Int] { x => Spork[Int => Int] { y => x + y }.build().apply(x) }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.strip()

  @Test
  def testInvalidCaptureMethodParameter(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        def fun(x: Int): PackedSpork[Int => Boolean] = Spork.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        object ShouldFail:
          def fun(x: Int): PackedSpork[Int => Boolean] = Spork.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.strip()

  val captureMeIfYouCan = 12

  @Test
  def testInvalidCaptureThis(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        class TestClass {
          Spork.apply { () => this.toString() }.build()
        }
        (new TestClass())
        """
      .contains:
        """
        Invalid capture of variable `TestClass`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        class Outer:
          val x = 12
          Spork.apply { () => 42 * x }.build()
        (new Outer())
        """
      .contains:
        """
        Invalid capture of `this` from class Some(Outer).
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        Spork.apply { () => 42 * captureMeIfYouCan }.build()
        """
      .contains:
        """
        Invalid capture of `this` from class Some(SporkLambdaTests).
        """.strip()
