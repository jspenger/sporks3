package sporks.example

import sporks.*
import sporks.given
import sporks.experimental.*


object OptExample {

  object Fun1 extends SporkObjectBuilder[Int => Int]({ x => x })
  object Fun2 extends SporkObjectBuilder[Int]({ 12 })
  object Fun3 extends SporkObjectBuilder[PackedSpork[Int] => Int]({ x => x.unwrap() })
  object Fun4 extends SporkObjectBuilder[Int ?=> Int]({ x ?=> x })

  def main(args: Array[String]): Unit = {
    val fun1 = Fun1.pack()
    val fun2 = Fun2.pack()

    val fun12 = fun1.withEnv2(fun2)
    println(fun12)
    println(fun12.unwrap())

    val fun3 = Fun3.pack()
    val fun23 = fun3.withEnv(fun2)
    println(fun23)
    println(fun23.compact())
    println(fun23.unwrap())

    val fun4 = Fun4.pack()
    val fun24 = fun4.withCtx2(fun2)
    println(fun24)
    println(fun24.unwrap())

    val spork1 = Fun1.build()
    val spork2 = Fun2.build()
    val spork3 = Fun3.build()
    val spork4 = Fun4.build()

    val spork12 = spork1.withEnv2(spork2)
    println(spork12)
    println(spork12.unwrap())
    val spork23 = spork3.withEnv(fun2)
    println(spork23)
    println(spork23.unwrap())
    val spork24 = spork4.withCtx2(spork2)
    println(spork24)
    println(spork24.unwrap())
  }
}
