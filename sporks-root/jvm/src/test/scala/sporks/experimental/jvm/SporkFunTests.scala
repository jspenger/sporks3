package sporks.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.experimental.*
import sporks.experimental.jvm.*

@RunWith(classOf[JUnit4])
class SporkFunTests:
  import SporkLambdaTests.*

  @Test
  def testSporkFunApply(): Unit =
    val predicate = SporkFun.apply[Int => Boolean] { x => x > 10 }
    val unwrapped = predicate.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    assertTrue(unwrapped(11))
    assertFalse(unwrapped(9))

    val higherLevelFilter = SporkFun.apply[PackedSpork[Int => Boolean] => Int => Option[Int]] { env => x => if env.build().apply(x) then Some(x) else None }
    val filter = higherLevelFilter.sporkfun_withEnv(predicate.packed)
    val unwrappedFilter = filter.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    assertEquals(Some(11), unwrappedFilter(11))
    assertEquals(None, unwrappedFilter(9))

  @Test
  def testSporkFunApplyWithEnv(): Unit =
    val fun9 = SporkFun.applyWithEnv(9) { x => x > 10 }
    val fun11 = SporkFun.applyWithEnv(11) { x => x > 10 }
    val unwrapped9 = fun9.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    val unwrapped11 = fun11.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    assertFalse(unwrapped9)
    assertTrue(unwrapped11)

  @Test
  def testSporkFunApplyWithCtx(): Unit =
    val fun9 = SporkFun.applyWithCtx(9) { summon[Int] > 10 }
    val fun11 = SporkFun.applyWithCtx(11) { summon[Int] > 10 }
    val unwrapped9 = fun9.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    val unwrapped11 = fun11.sporkfun_pack().sporkfun_unpack().sporkfun_unwrap()
    assertFalse(unwrapped9)
    assertTrue(unwrapped11)
