package sporks.example

import upickle.default.*

import sporks.*
import sporks.given
import sporks.jvm.*


object AutoCaptureExample {

  // The `AutoCapture.apply` method and its alias `spauto` does the following:
  //
  // 1. Lifts all captured symbols to parameters
  // 2. Find the implicit readwriters for each captured symbol
  // 3. Pack the new lifted function into a packed spork
  // 4. Pack the captured symbols together with their readwriters
  //
  // Consider the example:
  //
  // {{{
  //   def foo(x: Int, y: Int) = {
  //     spauto{ (i: Int) => x + y + i }
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
  // varibles and their readwriters.


  // A factory for a serialized function that checks if a number is between the
  // numbers `x` and `y`.
  def isBetween(x: Int, y: Int): Spork[Int => Boolean] = {
    AutoCapture.apply { (i: Int) => x <= i && i < y }
    // // optionally, we can use the `spauto` shorthand method:
    // spauto{ (i: Int) => x <= i && i < y }
  }


  // It is possible to create a custom data type and readwriter, for data that
  // is captured and packed. Here we create a custom `Range` data type, and its
  // corresponding readwriter.
  case class Range(x: Int, y: Int)
  object RangeRW extends SporkBuilder[ReadWriter[Range]]({ macroRW })
  given rangeRW: Spork[ReadWriter[Range]] = RangeRW.pack()

  // Now we can create a similar factory but by capturing a `Range` object.
  def isInRange(range: Range): Spork[Int => Boolean] = {
    // The `spauto` method will automatically pack the `Range` object and its
    // readwriter.
    spauto{ (i: Int) => range.x <= i && i < range.y }
  }


  // // If the readwriter is missing, then it is not possible to capture and
  // // pack the value. It will emit the following error:
  // // no implicit values were found that match type sporks.Spork[upickle.default.ReadWriter[sporks.experimental.example.AutoCaptureExample.Range2]]
  // case class Range2(x: Int, y: Int)
  // def isInRange2(range: Range2): Spork[Int => Boolean] = {
  //   spauto{ (i: Int) => range.x <= i && i < range.y }
  // }


  def main(args: Array[String]): Unit = {
    val btwn1020 = isBetween(10, 20)

    println(btwn1020)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(sporks.experimental.example.AutoCaptureExample$Lambda$1),PackedEnv(10,PackedObject(sporks.ReadWriters$IntRW$))),PackedEnv(20,PackedObject(sporks.ReadWriters$IntRW$)))

    println(btwn1020.unwrap().apply(5))
    // result: false

    println(btwn1020.unwrap().apply(15))
    // result: true

    println(btwn1020.unwrap().apply(25))
    // result: false

    val filter = AutoCapture.apply { (l: List[Int]) => l.filter(btwn1020.unwrap()) }
    // opt: val filter = spauto{ (l: List[Int]) => l.filter(btwn1020.unwrap()) }

    println(filter)
    // result: PackedWithEnv(PackedLambda(sporks.experimental.example.AutoCaptureExample$Lambda$3),PackedEnv({"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedLambda","fun":"sporks.experimental.example.AutoCaptureExample$Lambda$1"},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"10","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}},"packedEnv":{"$type":"sporks.Packed.PackedEnv","env":"20","rw":{"$type":"sporks.Packed.PackedObject","fun":"sporks.ReadWriters$IntRW$"}}},PackedObject(sporks.ReadWriters$SporkRW$)))

    println(filter.unwrap().apply(List(9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)))
    // result: List(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)

    val inRange = isInRange(Range(1, 2))

    println(inRange)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(sporks.experimental.example.AutoCaptureExample$Lambda$2),PackedEnv({"x":1,"y":2},PackedObject(sporks.experimental.example.AutoCaptureExample$RangeRW$))),PackedEnv(null,PackedObject(sporks.ReadWriters$UnitRW$)))

    println(inRange.unwrap().apply(0))
    // result: false

    println(inRange.unwrap().apply(1))
    // result: true

    println(inRange.unwrap().apply(2))
    // result: false
  }
}
