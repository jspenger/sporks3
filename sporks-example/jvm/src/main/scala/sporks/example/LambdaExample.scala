package sporks.example

import sporks.*
import sporks.given
import sporks.jvm.*
import sporks.example.platform.*


object LambdaExample {

  val Lambda1 = SporkBuilder.apply[Int => String] { x => x.toString.reverse }

  val Lambda2 = SporkBuilder.applyWithEnv[Int, Int => String](12) { env => x => (env + x).toString.reverse }

  val Lambda3 = SporkBuilder.apply[Option[Int] => Int] { x => x.map { _ + 1 }.getOrElse(0) }

  val Lambda4 = SporkBuilder.applyWithCtx[Int, Int](14) { summon[Int] }

  // // Should cause compile error
  // object ShouldFail:
  //   SporkBuilder.apply[Int => Int] { x =>
  //     SporkBuilder.apply[Int => Int] { y =>
  //       // Invalid capture of variable `x`. Use the first parameter of a spork's body to refer to the spork's environment.
  //       x + y
  //     }.unwrap().apply(x)
  //   }

  // // Should cause compile error
  // import upickle.default.*
  // def SporkFactoryFail[T: ReadWriter] = SporkBuilder.apply { summon[ReadWriter[T]] } // Invalid capture of variable `evidence$1`. Use the first parameter of a spork's body to refer to the spork's environment.


  def main(args: Array[String]): Unit = {
    println(
      Lambda1.unwrap().apply(10)
    )

    println(
      Lambda1.withEnv(100).unwrap()
    )

    println(
      Lambda2.withEnv(10).unwrap()
    )

    println(
      Lambda3.unwrap()(Some(10))
    )

    println(
      Lambda4.unwrap()
    )

    writeToFile(Lambda1, "Lambda1.json")
    println(
      readFromFile[PackedSpork[Int => String]]("Lambda1.json")
    )

    writeToFile(Lambda2, "Lambda2.json")
    println(
      readFromFile[PackedSpork[Int => String]]("Lambda2.json")
    )

    writeToFile(Lambda3, "Lambda3.json")
    println(
      readFromFile[PackedSpork[Option[Int] => Int]]("Lambda3.json")
    )

    writeToFile(Lambda3.withEnv(Some(42)), "Lambda3WithEnv.json")
    println(
      readFromFile[PackedSpork[Int]]("Lambda3WithEnv.json")
    )

  }
}
