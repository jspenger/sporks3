package sporks.example

import sporks.given
import sporks.*
import sporks.jvm.*
import sporks.example.platform.*

val Lambda1 = Spork[Int => String] { x => x.toString.reverse }

val Lambda2 = Spork.applyWithEnv[Int, Int => String](12) { env => x => (env + x).toString.reverse }

val Lambda3 = Spork[Option[Int] => Int] { x => x.map { _ + 1 }.getOrElse(0) }

// // Should cause compile error
// object ShouldFail:
//   Spork[Int => Int] { x =>
//     Spork[Int => Int] { y =>
//       // Invalid capture of variable `x`. Use first parameter of spore's body to refer to the spore's environment
//       x + y
//     }.build().apply(x)
//   }

// // Should cause compile error
// import upickle.default.*
// def SporkFactoryFail[T: ReadWriter] = Spork { summon[ReadWriter[T]] } // An owner of the provided builder is neither an object nor a package

object LambdaExample:
  def main(args: Array[String]): Unit =
    println:
      "Lambda1"
    println:
      Lambda1
    println:
      Lambda1.build()(10)

    println:
      "Lambda2"
    println:
      Lambda2
    println:
      Lambda2.build()(10)

    println:
      "Lambda3"
    println:
      Lambda3
    println:
      Lambda3.build()(Some(10))
    println:
      Lambda3.packWithEnv(Some(10)).build()

    println:
      writeToFile(Lambda1, "Lambda1.json")
      readFromFile[PackedSpork[Int => String]]("Lambda1.json")
    println:
      writeToFile(Lambda2, "Lambda2.json")
      readFromFile[PackedSpork[Int => String]]("Lambda2.json")
    println:
      writeToFile(Lambda3, "Lambda3.json")
      readFromFile[PackedSpork[Option[Int] => Int]]("Lambda3.json")
