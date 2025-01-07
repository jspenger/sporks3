package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkClassTests:
  object ShouldError:
    object NotClzClz extends SporkClass[Int => Int](x => x)

    def someMethod: SporkClass[Int => Int] = {
      class Local extends SporkClass[Int => Int](x => x)
      new Local()
    }

    class F[T]
    class ClassWithContex[T: F] extends SporkClass[F[T]] ( summon )

@RunWith(classOf[JUnit4])
class SporkClassTests:
  import SporkClassTests.*

  @Test
  def testObjectSporkClassError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ShouldError.NotClzClz.pack()
        """
      .contains:
        """
        The provided SporkClass `sporks.SporkClassTests.ShouldError.NotClzClz` is not a class.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notClzClz = ShouldError.NotClzClz
        notClzClz.pack()
        """
      .contains:
        """
        The provided SporkClass `notClzClz` is not a class.
        """.strip()

  @Test
  def testSporkClassWithContextParameterError(): Unit =
    // Catches a common mistake in which implicit parameters are used in the 
    // constructor. For example, this would seem like a reasonable thing to do, 
    // but will not work:
    // 
    // class PackedRW[T: ReadWriter] extends SporkClass[ReadWriter[T]](summon[ReadWriter[T]])
    // given PackedSpork[ReadWriter[T]] = PackedRW[T].pack()
    // 
    // // This will crash at runtime, as the init method is assumed to not have any params.
    // summon[PackedSpork[ReadWriter[Int]]].build() 

    assertTrue:
      typeCheckErrors:
        """
        given ShouldError.F[Int] = new ShouldError.F[Int]()
        ShouldError.ClassWithContex[Int].pack()
        """
      .contains:
        """
        The constructor of the provided SporkClass `ClassWithContex` `<init>` contains a context parameter list.
        """.strip()
