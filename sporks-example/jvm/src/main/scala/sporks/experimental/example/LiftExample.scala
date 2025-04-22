package sporks.experimental.example

import upickle.default.*

import sporks.*
import sporks.given
import sporks.experimental.*
import sporks.experimental.jvm.*


object LiftExample {

  // The `Lift.apply` method and its alias `spx` does the following:
  //
  // 1. LambdaLift: Lifts all captured symbols to parameters
  // 2. Find the implicit codecs for each captured symbol
  // 3. Pack the new lifted function into a packed spork
  // 4. Pack the captured symbols together with their codecs
  //
  // Consider the example:
  //
  // {{{
  //   def foo(x: Int, y: Int) = {
  //     spx { (i: Int) => x + y + i }
  //   }
  // }}}
  //
  // The closure
  //
  // {{{
  //  { (i: Int) => x + y + i }
  // }}}
  //
  // captures `x` and `y`. This is lifted in the first phase to:
  //
  // {{{
  //   (x: Int) => (y: Int) => (i: Int) => x + y + i)
  // }}}
  //
  // After that, it will pack the lifted function, and pack the captured
  // varibles and their codecs.


  // A factory for a serialized function that checks if a number is between the
  // numbers `x` and `y`.
  def isBetween(x: Int, y: Int): PackedSpork[Int => Boolean] = {
    Lift.apply { (i: Int) => x <= i && i < y }
    // // optionally, we can use the `spx` shorthand method:
    // spx { (i: Int) => x <= i && i < y }
  }


  // It is possible to create a custom data type and codec, for data that is
  // captured and packed. Here we create a custom `Range` data type, and its
  // corresponding codec.
  case class Range(x: Int, y: Int)
  object RangeRW extends SporkObjectBuilder[ReadWriter[Range]]({ macroRW })
  given rangeRW: PackedSpork[ReadWriter[Range]] = RangeRW.pack()

  // Now we can create a similar factory but by capturing a `Range` object.
  def isInRange(range: Range): PackedSpork[Int => Boolean] = {
    // The `spx` method will automatically pack the `Range` object and its
    // codec.
    spx { (i: Int) => range.x <= i && i < range.y }
  }


  // // If the codec is missing, then it is not possible to capture and pack the
  // // value. It will emit the following error:
  // // no implicit values were found that match type sporks.PackedSpork[upickle.default.ReadWriter[sporks.experimental.example.LiftExample.Range2]]
  // case class Range2(x: Int, y: Int)
  // def isInRange2(range: Range2): PackedSpork[Int => Boolean] = {
  //   spx { (i: Int) => range.x <= i && i < range.y }
  // }


  def main(args: Array[String]): Unit = {
    val btwn1020 = isBetween(10, 20)

    println(btwn1020)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(sporks.experimental.example.LiftExample$Lambda$1),PackedEnv(10,PackedObject(sporks.ReadWriters$IntRW$))),PackedEnv(20,PackedObject(sporks.ReadWriters$IntRW$)))

    println(btwn1020.unwrap().apply(5))
    // result: false

    println(btwn1020.unwrap().apply(15))
    // result: true

    println(btwn1020.unwrap().apply(25))
    // result: false

    val filter = Lift.apply { (l: List[Int]) => l.filter(btwn1020.unwrap()) }
    // opt: val filter = spx { (l: List[Int]) => l.filter(btwn1020.unwrap()) }

    println(filter)
    // result: PackedWithEnv(PackedLambda(sporks.experimental.example.LiftExample$Lambda$3),PackedEnv({"$type":"sporks.PackedSpork.PackedWithEnv","packed":{"$type":"sporks.PackedSpork.PackedWithEnv","packed":{"$type":"sporks.PackedSpork.PackedLambda","fun":"sporks.experimental.example.LiftExample$Lambda$1"},"packedEnv":{"$type":"sporks.PackedSpork.PackedEnv","env":"10","rw":{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}},"packedEnv":{"$type":"sporks.PackedSpork.PackedEnv","env":"20","rw":{"$type":"sporks.PackedSpork.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}},PackedObject(sporks.ReadWriters$PackedSporkRW$)))

    println(filter.unwrap().apply(List(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)))
    // result: List(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

    val inRange = isInRange(Range(1, 2))

    println(inRange)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(sporks.experimental.example.LiftExample$Lambda$2),PackedEnv({"x":1,"y":2},PackedObject(sporks.experimental.example.LiftExample$RangeRW$))),PackedEnv(null,PackedObject(sporks.ReadWriters$UnitRW$)))

    println(inRange.unwrap().apply(0))
    // result: false

    println(inRange.unwrap().apply(1))
    // result: true

    println(inRange.unwrap().apply(2))
    // result: false
  }
}
