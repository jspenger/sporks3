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

@RunWith(classOf[JUnit4])
class SporkClassTests:

  @Test
  def testObjectSporkClassError(): Unit =
    // The provided SporkClass `sporks.SporkObjectTests.ShouldError.NotClzClz` is not a class.
    assertTrue:
      typeCheckFail:
        """
        ShouldError.NotClzClz.pack()
        """

    // The provided SporkObject `notClzClz` is not a class.
    assertTrue:
      typeCheckFail:
        """
        val notClzClz = ShouldError.NotClzClz
        notClzClz.pack()
        """
