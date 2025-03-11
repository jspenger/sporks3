package sporks.experimental

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.experimental.*

@RunWith(classOf[JUnit4])
class SporkFunTests:
  import SporkObjectTests.*

  @Test
  def testSporkFun(): Unit =
    val filter = HigherLevelFilter
      .sporkfun_build()
      .sporkfun_uncurried2()
      .sporkfun_tupled2()
      .sporkfun_pack()
      .sporkfun_unpack()
      .sporkfun_untupled2()
      .sporkfun_curried2()
      .sporkfun_withEnv(
        Predicate
          .sporkfun_build()
          .sporkfun_pack()
      )
      .sporkfun_pack()
      .sporkfun_unpack()
      .sporkfun_unwrap()

    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))

  @Test
  def testSporkFunWithCtx(): Unit =
    val res = PredicateCtx
      .sporkfun_build()
      .sporkfun_withCtx(11)
      .sporkfun_pack()
      .sporkfun_unpack()
      .sporkfun_unwrap()

    assertTrue(res)

  @Test
  def testSporkFunReadWriter(): Unit =
    import SporkObjectTests.*

    val json = """{"$type":"sporks.PackedObject","fun":"sporks.SporkObjectTests$Predicate$"}"""

    val packed = upickle.default.write(Predicate.sporkfun_build().sporkfun_pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).sporkfun_unpack().sporkfun_unwrap()
    assertTrue(loaded(11))
    assertFalse(loaded(9))
