package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import upickle.default.*

import sporks.*
import sporks.given
import sporks.TestUtils.*


object AutoCaptureErrorTests {

  /** Foo values cannot be captured as there is no Spork[ReadWriter[Foo]]]. */
  case class Foo(x: Int, y: Int)

  /** Opaque type without a ReadWriter. */
  opaque type OpaqueInt = Int
  object OpaqueInt {
    def apply(value: Int): OpaqueInt = value
    def unwrap(value: OpaqueInt): Int = value
  }

  // For some reason this doesn't cause any errors when using the
  // `typeCheckErrors` method, but it does so here...
  // class Outer { outer =>
  //   val y = 12
  //   class Inner {
  //     def foo = spauto { (x: Int) => x + outer.y }
  //   }
  // }
}


@RunWith(classOf[JUnit4])
class AutoCaptureErrorTests {
  import AutoCaptureErrorTests.*

  @Test
  def testCaptureIdentError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        val foo = Foo(12, 13)
        spauto { foo }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        val foo = Foo(12, 13)
        spauto { (x: Int) => x + foo.x + foo.y }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        val foo = Foo(12, 13)
        spauto { def bar(x: Int): Int = { x + foo.x } }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureClassError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        class A(val a: Int)
        spauto { new A(12) }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        class A(val a: Int)
        spauto { (x: Int) => x + new A(12).a }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureMethodError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        def captureMeIfYouCan(): Int = 12
        spauto { (x: Int) => x + captureMeIfYouCan() }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `captureMeIfYouCan`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  val captureThisXIfYouCan = 99

  @Test
  def testCaptureThisError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        spauto { this }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        spauto { this.captureThisXIfYouCan }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        spauto { (x: Int) => x + captureThisXIfYouCan }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureImplicitThisError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        case class Bar(x: Int, y: Int)
        given ReadWriter[Bar] = macroRW[Bar]
        given Spork[ReadWriter[Bar]] = spauto { summon[ReadWriter[Bar]] }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `given_ReadWriter_Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureOpaqueTypeError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        val opaqueInt = OpaqueInt(12)
        spauto { (x: Int) => x + OpaqueInt.unwrap(opaqueInt) }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `opaqueInt`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedThisNestedClassError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        class Outer {
          class Inner {
            val y = 12
            def foo = spauto { (x: Int) => x + y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Inner`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        class Outer {
          val y = 12
          class Inner {
            def foo = spauto { (x: Int) => x + y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        class Outer {
          val y = 12
          class Inner {
            def foo = spauto { (x: Int) => x + Outer.this.y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedIdentInClassError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        val foo = Foo(12, 13)
        spauto {
          class Bar {
            def bar = foo.x + 12
          }
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedNewClassError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        class Bar(x: Int, y: Int)
        spauto { (x: Int) =>
          new Bar(12, 14)
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
        class Bar[T](x: T, y: T)
        spauto { (x: Int) =>
          new Bar[Int](12, 14)
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedClassExtendsError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
          class Bar0
          spauto {
            class FooBar extends Bar0
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar0`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
          class Bar1(x: Int, y: Int)
          spauto {
            class FooBar extends Bar1(12, 13)
          }
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar1`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
          class Bar2[T](x: T, y: T)
          spauto {
            class FooBar extends Bar2[Int](12, 13)
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar2`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
          val x = 12
          trait Bar3 { def bar: Int = x }
          spauto {
            class FooBar extends Foo(12, 13) with Bar3
          }
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar3`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrors:
        """
          trait Bar4[T] { def bar: Int = x }
          spauto {
            class FooBar extends Foo(12, 13) with Bar4[Int]
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar4`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedTraitExtendsError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        trait Bar
        spauto {
          trait FooBar extends Bar
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testObjectExtendsCapturedError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        trait Bar
        spauto {
          object FooBar extends Bar
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedEnumError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        enum Bar { case Baz }
        spauto { Bar.Baz }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedUnapplyError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        sealed trait Bar
        case class Baz(x: Int, y: Int) extends Bar
        spauto { (x: Bar) => x match {
            case Baz(a, b) => a + b
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Baz`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedThisSuperError(): Unit = {
    assertTrue:
      typeCheckErrors:
        """
        class Bar extends Foo(12, 13) {
          def bar = spauto { (x: Int) => x.toString() + super.toString() }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type sporks.Spork\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }
}
