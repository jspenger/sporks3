# Sporks3

Simple and safe serialization/pickling library for functions/closures in Scala 3.

This is an experimental fork of the "Spores3" project created by Philipp Haller ([https://github.com/phaller/spores3](https://github.com/phaller/spores3)), which in turn is a continuation of the work on "Spores" by Heather Miller and Philipp Haller.
This fork is heavily modified to fit another project that uses Sporks3 for serialization of closures.

<picture>
  <source media="(prefers-color-scheme: dark)"  srcset="./sporky2.png">
  <source media="(prefers-color-scheme: light)" srcset="./sporky1.png">
  <img src="./sporky1.png" width="70" />
</picture>

## Project Overview

Sporks make it safe to create, serialize, deserialize, and unwrap *closures*.
Any issues with your code that would break this promise will cause a compiler error, it is guaranteed to not throw runtime errors.

Using Sporks will prevent you from creating closures which are not serializable.
```scala
class Foo {
  val y = 42
  val fun = Spork.apply{ (x: Int) => { x + y } }
  //                                       ^
  // Error: Invalid capture of `this` from class Foo.
}
```

Writing the same program using Java's own serialization will compile but throw a `NotSerializableException` at runtime when serializing an instance of `fun`, as `this.y` is captured and `this` is not `Serializable`.

We can create a `Spork[Int => Int]` which safely captures `y` in four ways.

Option 1: Passing `y` as an explicit environment variable with the Spork factory. (JVM only)
```scala
val fun1 = Spork.apply({ (y: Int) => (x: Int) => { x + y } }).withEnv(y)
val fun2 = Spork.applyWithEnv(y) { (y: Int) => (x: Int) => { x + y } }
val fun3 = Spork.applyWithCtx(y) { (y: Int) ?=> (x: Int) => { x + y } }
```

Option 2: Automatically checking and capturing environment variables. This requires an implicit `Spork[ReadWriter[T]]` in scope, where `T` is the type of the captured variable. (JVM only)
```scala
val fun4 = AutoCapture.apply{ (x: Int) => { x + y } }
````

Option 3: Using a `SporkBuilder`, by extending the `SporkBuilder` trait from a top-level object. (JVM, ScalaJS, ScalaNative)
```scala
object MyFun5 extends SporkBuilder[Int => Int => Int]({ y => x => x + y })
...
val fun5 = MyFun5.pack().withEnv(y)
```

Option 4: Using a `SporkClassBuilder`, by extending the `SporkClassBuilder` trait from a top-level class. (JVM, ScalaJS, ScalaNative)
```scala
class MyFun6[T] extends SporkClassBuilder[T => T => Int]({ y => x => x.toString().length() + y.toString().length() })
...
val fun6 = new MyFun6[Int]().pack().withEnv(y)
```

All examples above will create a `Spork[Int => Int]`.
The easiest way to serialize and deserialize a Spork is to use the upickle library.
```scala
val serialized: String = upickle.default.write(fun1)
val deserialized: Spork[Int => Int] = upickle.default.read[Spork[Int => Int]](serialized)
```

Use the `unwrap` method to unwrap the wrapped closure in a Spork.
```scala
val unwrapped: Int => Int = deserialized.unwrap()
```

Partially apply a Spork to an environment variable using the `withEnv` or `withCtx` methods.
It can also be partially applied directly to a Spork using the `withEnv2` or `withCtx2` methods.
```scala
val fun7 = Spork.apply[Int ?=> Int => Int] { x ?=> y => x + y }
val fun7WithCtx = fun7.withCtx(42)
val fun7WithCtxWithEnv = fun7WithCtx.withEnv(23)
fun7WithCtxWithEnv.unwrap() //65
```

## Packing Environment Variables

The `withEnv` and `withCtx` methods, as well as the `AutoCapture` factory, pack environment variables into the Spork.
Packing an environment variable of type `T` requires that a `Spork[ReadWriter[T]]` is available in the contextual scope.
This way, the environment variable is packed together with a serialized/pickled serializer/pickler for its type.
The most common packed picklers are already available and can be imported by `import sporks.given`.
You can also create your own packed picklers, examples of this are in [sporks-root/shared/src/main/scala/sporks/ReadWriters.scala](sporks-root/shared/src/main/scala/sporks/ReadWriters.scala).

## Examples

Import sporks3 into your project.

```scala
import sporks.*
import sporks.given
import sporks.jvm.*
```

SporkBuilders are the most robust way to create sporks.
```scala
object Predicate
    extends SporkBuilder[Int => Boolean]({ x =>
      x > 10
    })

object Filter
    extends SporkBuilder[
      (Int => Boolean) => Int => Boolean
    ]({ env => x =>
      env.apply(x)
    })

val predicate = Predicate.pack()
val filter    = Filter.pack().withEnv2(predicate)
val fun       = filter.unwrap()
fun(11) // true
fun(9) // false
```

Sporks can be serialized/pickled and deserialized/unpickled by using the upickle library's ReadWriter.
```scala
import upickle.default.* // imports: read, write, etc.

// ...
val filter    = Filter.pack().withEnv2(predicate)
val pickled   = write(filter) // {"$type":"sporks.Packed.PackedWithEnv","packed":{"$type":"sporks.Packed.PackedObject","fun":"sporks.example.LambdaExample$Filter$"},"packedEnv":{"$type":"sporks.Packed.PackedObject","fun":"sporks.example.LambdaExample$Predicate$"}}
val unpickled = read[Spork[Int => Boolean]](pickled)
val fun       = unpickled.unwrap()
fun(11) // true
fun(9) // false
```

Lambdas from the Spork factory are the most convenient way to create sporks.
However, they are only supported on the JVM.
```scala
val predicate = Spork.apply[Int => Boolean]({ x => x > 10 })
val filter =
  Spork.apply[
    (Int => Boolean) => Int => Boolean
  ]({ env => x =>
    env.apply(x)
  })
val fun = filter.withEnv2(predicate).unwrap()
fun(11) // true
fun(9) // false
```

The SporkClassBuilder builder is the most flexible way to create sporks.
However, it is not recommended to use it unless you have a good reason to do so.
```scala
class Constant[T] extends SporkClassBuilder[T => T]({ env => env })
val constant = new Constant[Int]().pack().withEnv(42)
constant.unwrap() // 42
```

You can find more examples in the [sporks-example](sporks-example) directory.
