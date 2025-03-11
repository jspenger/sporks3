package sporks.example

import sporks.*
import sporks.given
import sporks.example.utils.*
import sporks.experimental.*

object Greeter
    extends SporkObject[String => String => String]({ greeting => name =>
      s"$greeting, $name!"
    })

object SporkFunGreeter {
  def main(args: Array[String]): Unit = {
    Greeter
      .sporkfun_build()
      .sporkfun_unwrap()
      .apply("Howdy")
      .apply("Partner")
      .tap(str => println(s"Result: $str\n"))

    Greeter
      .sporkfun_build()
      .sporkfun_pack()
      .tap(str => println(s"Packed: $str\n"))
      .sporkfun_unpack()
      .sporkfun_withEnv("Hello")
      .sporkfun_withEnv("Traveler")
      .sporkfun_pack()
      .tap(str => println(s"Packed again: $str\n"))
      .sporkfun_unpack()
      .sporkfun_unwrap()
      .tap(str => println(s"Another result: $str\n"))
  }
}
