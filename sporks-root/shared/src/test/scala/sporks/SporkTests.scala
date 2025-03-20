package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*


object SporkTests:
  object Thunk extends SporkObjectBuilder[() => Int](() => 10)

  class Flatten[T] extends SporkClassBuilder[List[List[T]] => List[T]](x => x.flatten)


@RunWith(classOf[JUnit4])
class SporkTests:
  import SporkTests.*

  @Test
  def testBuildPackUnpackUnwrapObject(): Unit =
    val built = Thunk.build()
    val packed = built.pack()
    val unpacked = packed.unpack()
    val unwrapped = unpacked.unwrap()
    assertEquals(10, unwrapped())

  @Test
  def testBuildPackUnpackUnwrapClass(): Unit =
    val built = new Flatten[Int].build()
    val packed = built.pack()
    val unpacked = packed.unpack()
    val unwrapped = unpacked.unwrap()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, unwrapped(nestedList))
