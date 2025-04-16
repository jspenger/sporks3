package sporks.example

import sporks.*
import sporks.given
import sporks.example.platform.*


object Example {

  object Spork1 extends SporkObjectBuilder[Int => String](x => x.toString.reverse)

  object Spork2 extends SporkObjectBuilder[Int => Int => String](env => x => (env + x).toString.reverse)

  object Predicate extends SporkObjectBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporkObjectBuilder[PackedSpork[Int => Boolean] => Int => Boolean](env => x => env.unwrap().apply(x))

  object SporkOption extends SporkObjectBuilder[Option[Int] => Int => String](env => x => env.map(_ + x).map(_.toString.reverse).getOrElse(""))

  object SporkWithCtx extends SporkObjectBuilder[Int ?=> String](summon[Int].toString().reverse)

  class Constant[T] extends SporkClassBuilder[T => T](x => x)


  def main(args: Array[String]): Unit = {
    // `pack` the `SporkObjectBuilder` to get a `PackedSpork` and `unwrap` it to
    // reveal the packed function.
    println(
      Spork1.pack().unwrap()(10)
    )

    // `withEnv` to pack an environment into the `PackedSpork`.
    println(
      Spork2.pack().withEnv(11).unwrap()(10)
    )

    // The resulting `PackedSpork` is a simple data structure.
    println(
      Spork2.pack().withEnv(11)
    )

    // `build` to get a `Spork` which is not serialized unlike the
    // `PackedSpork`.
    println(
      Spork2.build().withEnv(11)
    )

    // A `Spork` can also be packed, and later unpacked to regain the original.\
    println(
      Spork2.build().withEnv(11).pack().unpack()
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

    // ... Although, try to stick to the `SporkObjectBuilder` for performance
    // reasons. Their main use case can be found in package.scala.

    // Another method for creating sporks is by using the `SporkBuilder.apply`
    // method. This can be imported from the `sporks.jvm` package, as it is
    // only available on the JVM. It is a convenient method for creating
    // spork lambdas. See the example in LambdaExample.scala.
    //
    // val lambda = SporkBuilder.apply[Int => String] { x => x.toString.reverse }

    // A `PackedSpork` can be serialized/pickled to JSON by using `upickle`.
    writeToFile(Spork1.pack(), "Spork1.json")
    println(
      readFromFile[PackedSpork[Int => String]]("Spork1.json")
    )

    writeToFile(Spork2.pack().withEnv(10), "Spork2.json")
    println(
      readFromFile[PackedSpork[Int => String]]("Spork2.json")
    )

    writeToFile(HigherLevelFilter.pack().withEnv(Predicate.pack()), "Filter.json")
    println(
      readFromFile[PackedSpork[Int => Boolean]]("Filter.json")
    )
    println(
      readFromFile[PackedSpork[Int => Boolean]]("Filter.json").unwrap().apply(12)
    )

    // Similarly, a `Spork` can be serialized/pickled to JSON by using
    // `upickle`.
    writeToFile(Spork1.build(), "Spork11.json")
    println(
      readFromFile[Spork[Int => String]]("Spork11.json")
    )
  }
}
