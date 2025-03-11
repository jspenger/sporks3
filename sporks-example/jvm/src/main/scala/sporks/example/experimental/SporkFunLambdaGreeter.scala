package sporks.example.experimental

import sporks.*
import sporks.given
import sporks.example.utils.*
import sporks.experimental.*
import sporks.experimental.jvm.*

object SporkFunLambdaGreeter {
  def main(args: Array[String]): Unit = {
    val spork = SporkFun.apply[String => String => String] { greeting => name =>
      s"$greeting, $name!"
    }

    spork
      .sporkfun_withEnv("Howdy")
      .sporkfun_withEnv("Jonas")
      .sporkfun_pack()
      .tap(str => println(s"Packed: $str\n"))
      .sporkfun_unpack()
      .sporkfun_unwrap()
      .tap(str => println(s"Result: $str\n"))

    spork
      .sporkfun_withEnv("Bonjour")
      .sporkfun_withEnv("Philipp")
      .sporkfun_unwrap()
      .tap(str => println(s"Another result: $str\n"))

    spork
      .sporkfun_uncurried2()
      .sporkfun_tupled2()
      .sporkfun_withEnv("Hello", "Traveler")
      .sporkfun_pack()
      .tap(str => println(s"Yet another packed: $str\n"))
      .sporkfun_unpack()
      .sporkfun_unwrap()
      .tap(str => println(s"Yet another result: $str\n"))

    SporkFun
      .apply[String ?=> String] { summon }
      .sporkfun_withCtx("I am the context!")
      .sporkfun_pack()
      .tap(str => println(s"Context packed: $str\n"))
      .sporkfun_unpack()
      .sporkfun_unwrap()
      .tap(str => println(s"Context result: $str\n"))
  }
}
