package sporks.example

import sporks.*
import sporks.given
import sporks.example.platform.*


object Example {

  object Spork1 extends SporkBuilder[Int => String](x => x.toString.reverse)

  object Spork2 extends SporkBuilder[Int => Int => String](env => x => (env + x).toString.reverse)

  object Predicate extends SporkBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporkBuilder[Spork[Int => Boolean] => Int => Boolean](env => x => env.unwrap().apply(x))

  object SporkOption extends SporkBuilder[Option[Int] => Int => String](env => x => env.map(_ + x).map(_.toString.reverse).getOrElse(""))

  object SporkWithCtx extends SporkBuilder[Int ?=> String](summon[Int].toString().reverse)

  class Constant[T] extends SporkClassBuilder[T => T](x => x)


  def main(args: Array[String]): Unit = {
    // `pack` the `SporkBuilder` to get a `Spork` and `unwrap` it to
    // reveal the packed function.
    println(
      Spork1.pack().unwrap()(10)
    )

    // `withEnv` to pack an environment into the `Spork`.
    println(
      Spork2.pack().withEnv(11).unwrap()(10)
    )

    // The resulting `Spork` is a simple data structure.
    println(
      Spork2.pack().withEnv(11)
    )

    // Higher order sporks can pack other sporks in their environment.
    println(
      HigherLevelFilter.pack().withEnv(Predicate.pack()).unwrap()(11)
    )

    // Besides primitive types, standard library types like `Option` can also be
    // packed in the environment.
    println(
      SporkOption.pack().withEnv(Some(10)).unwrap()(13)
    )

    // The environment paramter can also be a context parameter. A context
    // parameter can be packed using the `withCtx` method.
    println(
      SporkWithCtx.pack().withCtx(99).unwrap()
    )

    // The `SporkClassBuilder` can be used to create sporks with type
    // parameters.
    println({
      val constant10 = new Constant[Int]().pack().withEnv(10)
      constant10.unwrap()
    })

    // ... Although, try to stick to the `SporkBuilder` for performance
    // reasons. Their main use case can be found in package.scala.

    // Another method for creating sporks is by using the `Spork.apply`
    // method. This can be imported from the `sporks.jvm` package, as it is
    // only available on the JVM. It is a convenient method for creating
    // spork lambdas. See the example in LambdaExample.scala.
    //
    // val lambda = Spork.apply[Int => String] { x => x.toString.reverse }

    // A `Spork` can be serialized/pickled to JSON by using `upickle`.
    writeToFile(Spork1.pack(), "Spork1.json")
    println(
      readFromFile[Spork[Int => String]]("Spork1.json")
    )

    writeToFile(Spork2.pack().withEnv(10), "Spork2.json")
    println(
      readFromFile[Spork[Int => String]]("Spork2.json")
    )

    writeToFile(HigherLevelFilter.pack().withEnv(Predicate.pack()), "Filter.json")
    println(
      readFromFile[Spork[Int => Boolean]]("Filter.json")
    )
    println(
      readFromFile[Spork[Int => Boolean]]("Filter.json").unwrap().apply(12)
    )
  }
}
