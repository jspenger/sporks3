package sporks.example

import sporks.given
import sporks.*
import sporks.example.utils.*

object Spork1 extends SporkObject[Int => String](x => x.toString.reverse)

object Spork2
    extends SporkObject[Int => Int => String](env =>
      x => (env + x).toString.reverse
    )

object Predicate extends SporkObject[Int => Boolean](x => x > 10)

object HigherLevelFilter
    extends SporkObject[PackedSpork[Int => Boolean] => Int => Boolean](env =>
      x => env.build().apply(x)
    )

object SporkOption
    extends SporkObject[Option[Int] => Int => String](env =>
      x => env.map(_ + x).map(_.toString.reverse).getOrElse("")
    )

object SporkOption2
    extends SporkObject[Option[Int] => Int => String](env =>
      x => env.map(_ + x).map(_.toString.reverse).getOrElse("")
    )

object BuilderExample:
  def main(args: Array[String]): Unit =
    println:
      "Spork1"
    println:
      Spork1.pack()
    println:
      Spork1.pack().build()(10)

    println:
      "Spork2"
    println:
      Spork2
    println:
      Spork2.pack().packWithEnv(11)
    println:
      Spork2.pack().packWithEnv(11).build()(10)

    println:
      "HigherLevelFilter"
    println:
      HigherLevelFilter.pack().packWithEnv(Predicate.pack())
    println:
      HigherLevelFilter.pack().packWithEnv(Predicate.pack()).build()(9)
    println:
      HigherLevelFilter.pack().packWithEnv(Predicate.pack()).build()(11)

    println:
      writeToFile(Spork1.pack(), "Spork1.json")
      readFromFile[PackedSpork[Int => String]]("Spork1.json")
    println:
      writeToFile(Spork2.pack().packWithEnv(10), "Spork2.json")
      readFromFile[PackedSpork[Int => String]]("Spork2.json")

    println:
      "SporkOption"
    println:
      SporkOption.pack().packWithEnv(Some(10)).build()(13)
    println:
      SporkOption2.pack().packWithEnv(Some(10)).build()(13)
