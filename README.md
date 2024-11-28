# Sporks3

Simple and safe serialization/pickling library for closures in Scala 3.

This is an experimental fork of the "Spores3" project created by Philipp Haller ([https://github.com/phaller/spores3](https://github.com/phaller/spores3)), which in turn is a continuation of the work on "Spores" by Heather Miller and Philipp Haller.
This fork is heavily modified to fit another project that uses Sporks3 for serialization of closures.
As such, it is not yet intended for general use, and will likely change significantly over the next few months.

[![Build Status](https://github.com/jspenger/sporks3/actions/workflows/root-build-test.yaml/badge.svg)](https://github.com/jspenger/sporks3/actions/workflows/root-build-test.yaml)
[![Build Status](https://github.com/jspenger/sporks3/actions/workflows/examples-build-test.yaml/badge.svg)](https://github.com/jspenger/sporks3/actions/workflows/examples-build-test.yaml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/jspenger/sporks3/blob/main/LICENSE)

## Project Overview

Pickle and unpickle your closures.
There are three ways in which you can create a spork: 
- as an object, extending the `SporkObject` trait
- as a class, extending the `SporkClass` trait
- or, as a lambda, using the `Spork.apply` lambda factory (JVM only)

Using any of these three methods, you can create a `PackedSpork` object, by calling the `pack` method on a SporkObject or SporkClass object, or by using the lambda factory directly.
```scala
SporkObject[T](fun: T) -- pack() --> PackedSpork[T]
SporkClass [T](fun: T) -- pack() --> PackedSpork[T]
Spork.apply[T](fun: T) ------------> PackedSpork[T]
```

A PackedSpork can be used to `build` the closure. 
```scala
PackedSpork[T] -- build() --> T
```

Additionally, a PackedSpork can be partially applied to a serializable `env`ironment variable, using either the `packWithEnv` or the `packWithCtx` methods.
```scala
PackedSpork[E  => T] -- packWithEnv(env: E) --> PackedSpork[T]
PackedSpork[E ?=> T] -- packWithCtx(env: E) --> PackedSpork[T]
```

## Example

Import sporks3 into your project.

```scala
import sporks.*
import sporks.given
import sporks.jvm.*
```

SporkObjects are the most robust way to create sporks.
```scala
object Predicate
    extends SporkObject[Int => Boolean]({ x =>
      x > 10
    })

object Filter
    extends SporkObject[
      PackedSpork[Int => Boolean] => Int => Boolean
    ]({ env => x =>
      env.build().apply(x)
    })

val predicate = Predicate.pack()
val filter    = Filter.pack().packWithEnv(predicate)
val fun       = filter.build()
fun(11) // true
fun(9) // false
```

PackedSporks can be serialized/pickled and deserialized/unpickled by using the upickle library's ReadWriter.
```scala
import upickle.default.* // imports: read, write, etc.

// ...
val filter    = Filter.pack().packWithEnv(predicate)
val pickled   = write(filter) // "PackedWithEnv(PackedObject(sporks.Filter$),{"$type":"sporks.PackedObject","fun":"sporks.Predicate$"},PackedClass(sporks.package$PACKED_OBJECT_RW_T))"
val unpickled = read[PackedSpork[Int => Boolean]](pickled)
val fun       = unpickled.build()
fun(11) // true
fun(9) // false
```

Lambdas from the Spork factory are the most convenient way to create sporks.
However, they are only supported on the JVM.
```scala
val predicate = Spork.apply[Int => Boolean]({ x => x > 10 })
val filter =
  Spork.apply[
    PackedSpork[Int => Boolean] => Int => Boolean
  ]({ env => x =>
    env.build().apply(x)
  })
val fun = filter.packWithEnv(predicate).build()
fun(11) // true
fun(9) // false
```

The SporkClass builder is the most flexible way to create sporks.
However, it is not recommended to use it unless you have a good reason to do so.
```scala
class Constant[T] extends SporkClass[T => T]({ env => env })
val constant = new Constant[Int]().pack().packWithEnv(42)
constant.build() // 42
```

The SporkClass builder is useful for leveraging type parameters on JS and Native (where spork lambdas are not supported), or for creating spork combinators (see an example below for packing environment variables).
However, use the SporkObject instead if you can.

You can find more examples in the [sporks-example](sporks-example) directory.

## Packing Environment Variables

Packing an environment variable of type `T` requires that a `PackedSpork[ReadWriter[T]]` is available in the contextual scope.
This way, the environment variable is packed together with a serialized/pickled serializer/pickler for its type.
The most common packed picklers are already available and can be imported by `import sporks.given`.

You can create your own packed picklers. You can find examples of this in [sporks-root/shared/src/main/scala/sporks/package.scala](sporks-root/shared/src/main/scala/sporks/package.scala)

## Common Errors

- `ClassNotFoundException`, `java.lang.NoSuchMethodException: [...].<init>()`, `java.lang.NoSuchFieldException: MODULE$`
  - Case 1. 
    You will encounter this error when trying to `build` your closure.
    You have likely nested a SporkObject or a SporkClass definition inside a method call or similar.
    A SporkObject must either be top-level objects or nested within objects.
    A SporkClass must be not be a local class, i.e. not inside a method.
    You can find more info at: [https://github.com/portable-scala/portable-scala-reflect](https://github.com/portable-scala/portable-scala-reflect)
  - Case 2.
    You have extended a SporkObject from a class, or you have extended a SporkClass from an object.
    Make sure that for your object you extend the SporkObject trait and vice versa.

## Roadmap

- Add the `Duplicable` trait from Spores3, together with weaker and stronger sporks.
- Remove the type parameters on PackedSpork[T], PackedObject[T], etc., and replace with type members instead. This will enable the use of `SporkObject`s instead of `SporkClass`es for the packed serializers/picklers of `PackedSpork`s.
- Remove the direct dependency on upickle, make it work with any type-class-based serialization library.
- Increase the robustness of compile-time checks. There are some errors which are currently not caught.
