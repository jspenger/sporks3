package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

// // The following code should produce a compile error:
// // Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.bloop
// // ... but reproducing it with the typeCheckErrors macro is not possible as the object needs to be non-nested top-level.
// object Issue001:
//   def foo(x: Int): Spork[Int => Boolean] = Spork.apply[Int => Boolean] { y => y > x }

@RunWith(classOf[JUnit4])
class SporkLambdaErrorTests:

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
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        Spork.apply[Int => Int] { x => Spork.apply[Int => Int] { y => x + y }.unwrap().apply(x) }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.trim()

  @Test
  def testInvalidCaptureMethodParameter(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        def fun(x: Int): Spork[Int => Boolean] = Spork.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        object ShouldFail:
          def fun(x: Int): Spork[Int => Boolean] = Spork.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
        """.trim()

  val captureMeIfYouCan = 12

  @Test
  def testInvalidCaptureThis(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        class TestClass {
          Spork.apply { () => this.toString() }.unwrap()
        }
        (new TestClass())
        """
      .contains:
        """
        Invalid capture of `this` from outer class.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        class Outer:
          val x = 12
          Spork.apply { () => 42 * x }.unwrap()
        (new Outer())
        """
      .contains:
        """
        Invalid capture of `this` from class Outer.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        Spork.apply { () => 42 * captureMeIfYouCan }.unwrap()
        """
      .contains:
        """
        Invalid capture of `this` from class SporkLambdaErrorTests.
        """.trim()
