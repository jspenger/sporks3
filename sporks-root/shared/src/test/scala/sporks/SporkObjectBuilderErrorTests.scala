package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkObjectBuilderErrorTests:
  class NotObjObj extends SporkObjectBuilder[Int => Int](x => x)

  class SomeClass:
    object NotTopLevel extends SporkObjectBuilder[Int => Int](x => x)

  def someMethod: SporkObjectBuilder[Int => Int] = {
    object NotTopLevel extends SporkObjectBuilder[Int => Int](x => x)
    NotTopLevel
  }

@RunWith(classOf[JUnit4])
class SporkObjectBuilderErrorTests:
  import SporkObjectBuilderErrorTests.*

  @Test
  def testClassSporkObjectBuilderError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        new NotObjObj().pack()
        """
      .contains:
        """
        The provided SporkObjectBuilder `sporks.SporkObjectBuilderErrorTests$.NotObjObj` is not an object.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notObjObj = new NotObjObj()
        notObjObj.pack()
        """
      .contains:
        """
        The provided SporkObjectBuilder `sporks.SporkObjectBuilderErrorTests$.NotObjObj` is not an object.
        """.strip()

  @Test
  def testNotTopLevelError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        val notTopLevel = new SomeClass().NotTopLevel
        notTopLevel.pack()
        """
      .contains:
        """
        The provided SporkObjectBuilder `sporks.SporkObjectBuilderErrorTests$.SomeClass.NotTopLevel$` is not a top-level object; its owner `SomeClass` is not a top-level object nor a package.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notObject = someMethod
        notObject.pack()
        """
      .contains:
        """
        The provided SporkObjectBuilder `sporks.SporkObjectBuilder` is not an object.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        object Builder extends SporkObjectBuilder[Int => String](x => x.toString.reverse)
        Builder.pack()
        """
      .contains:
        """
        The provided SporkObjectBuilder `sporks.SporkObjectBuilderErrorTests._$Builder$` is not a top-level object; its owner `testNotTopLevelError` is not a top-level object nor a package.
        """.strip()
