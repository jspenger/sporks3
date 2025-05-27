package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkBuilderErrorTests:
  class NotObjObj extends SporkBuilder[Int => Int](x => x)

  class SomeClass:
    object NotTopLevel extends SporkBuilder[Int => Int](x => x)

  def someMethod: SporkBuilder[Int => Int] = {
    object NotTopLevel extends SporkBuilder[Int => Int](x => x)
    NotTopLevel
  }

@RunWith(classOf[JUnit4])
class SporkBuilderErrorTests:
  import SporkBuilderErrorTests.*

  @Test
  def testClassSporkBuilderError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        new NotObjObj().pack()
        """
      .contains:
        """
        The provided SporkBuilder `sporks.SporkBuilderErrorTests$.NotObjObj` is not an object.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        val notObjObj = new NotObjObj()
        notObjObj.pack()
        """
      .contains:
        """
        The provided SporkBuilder `sporks.SporkBuilderErrorTests$.NotObjObj` is not an object.
        """.trim()

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
        The provided SporkBuilder `sporks.SporkBuilderErrorTests$.SomeClass.NotTopLevel$` is not a top-level object; its owner `SomeClass` is not a top-level object nor a package.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        val notObject = someMethod
        notObject.pack()
        """
      .contains:
        """
        The provided SporkBuilder `sporks.SporkBuilder` is not an object.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        object Builder extends SporkBuilder[Int => String](x => x.toString.reverse)
        Builder.pack()
        """
      .exists:
        _.matches:
          raw"""
          The provided SporkBuilder `.*Builder\$$` is not a top-level object; its owner `.*` is not a top-level object nor a package.
          """.trim()
