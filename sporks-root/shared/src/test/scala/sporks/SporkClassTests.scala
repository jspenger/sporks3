package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkClassTests:
  class Thunk[T] extends SporkClass[T => () => T](t => () => t)

  class Predicate extends SporkClass[Int => Boolean](x => x > 10)

  class FilterWithTypeParam[T] extends SporkClass[PackedSpork[T => Boolean] => T => Option[T]]({ env => x => if env.build().apply(x) then Some(x) else None })

  class Flatten[T] extends SporkClass[List[List[T]] => List[T]](x => x.flatten)

  object NestedBuilder:
    class Predicate extends SporkClass[Int => Boolean](x => x > 10)

  object ShouldError:
    object NotClzClz extends SporkClass[Int => Int](x => x)

    def someMethod: SporkClass[Int => Int] = {
      class Local extends SporkClass[Int => Int](x => x)
      new Local()
    }

    class NestedBuilderInClass:
      class Inner extends SporkClass[Int](10)

    class ClassWithoutPublicConstructor private () extends SporkClass[Int => Int](x => x)
    object ClassWithoutPublicConstructor:
      def apply(): ClassWithoutPublicConstructor = new ClassWithoutPublicConstructor()

    class ClassWithParameters(i: Int) extends SporkClass[() => Int]( () => i )

    class F[T]
    class ClassWithContex[T: F] extends SporkClass[F[T]] ( summon )

@RunWith(classOf[JUnit4])
class SporkClassTests:
  import SporkClassTests.*

  @Test
  def testSporkClassPack(): Unit =
    val packed = new Predicate().pack()
    val predicate = packed.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))

  @Test
  def testSporkClassWithEnv(): Unit =
    val packed = new Thunk[Int].pack().packWithEnv(10)
    val thunk = packed.build()
    assertEquals(10, thunk())

  @Test
  def testSporkClassWithTypeParam(): Unit =
    val packed = new Flatten[Int].pack()
    val flatten = packed.build()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, flatten(nestedList))

  @Test
  def testHigherLevelSporkClass(): Unit =
    val packed = new FilterWithTypeParam[Int].pack()
    val filter = packed.build()
    val predicate = new Predicate().pack()
    assertEquals(Some(11), filter(predicate)(11))
    assertEquals(None, filter(predicate)(9))

  @Test
  def testPackedSporkClassReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedClass","fun":"sporks.SporkClassTests$Predicate"}"""

    val packed = upickle.default.write(new Predicate().pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Boolean]](json).build()
    assertTrue(loaded(11))
    assertFalse(loaded(9))

  @Test
  def testPackedSporkClassWithTypeParamReadWriter(): Unit =
    val json = """{"$type":"sporks.PackedClass","fun":"sporks.SporkClassTests$Flatten"}"""

    val packed = upickle.default.write(new Flatten[Int].pack())
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[List[List[Int]] => List[Int]]](json).build()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, loaded(nestedList))

  @Test
  def testPackedSporkClassWithEnv(): Unit =
    val json = """{"$type":"sporks.PackedWithEnv","packed":{"$type":"sporks.PackedClass","fun":"sporks.SporkClassTests$FilterWithTypeParam"},"env":"{\"$type\":\"sporks.PackedClass\",\"fun\":\"sporks.SporkClassTests$Predicate\"}","envRW":{"$type":"sporks.PackedObject","fun":"sporks.package$PACKED_CLASS_RW$"}}"""

    val predicate = new Predicate().pack()
    val filter = new FilterWithTypeParam[Int].pack().packWithEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[PackedSpork[Int => Option[Int]]](json).build()
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))

  @Test
  def testObjectSporkClassError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ShouldError.NotClzClz.pack()
        """
      .contains:
        """
        The provided SporkClass `sporks.SporkClassTests.ShouldError.NotClzClz` is not a class.
        """.strip()

    assertTrue:
      typeCheckErrors:
        """
        val notClzClz = ShouldError.NotClzClz
        notClzClz.pack()
        """
      .contains:
        """
        The provided SporkClass `notClzClz` is not a class.
        """.strip()

  @Test
  def testSporkClassNestedInClassError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        val builder = new ShouldError.NestedBuilderInClass()
        val pred = new builder.Inner()
        pred.pack()
        """
      .contains:
        """
        The provided SporkClass `Inner` is nested in a class.
        """.strip()

  @Test
  def testSporkClassNestedInMethodError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ShouldError.someMethod.pack()
        """
      .contains:
        """
        The provided SporkClass `SporkClass` is not a concrete class.
        """.strip()
  
  @Test
  def testSporkClassWithPrivateConstructorError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ShouldError.ClassWithoutPublicConstructor().pack()
        """
      .contains:
        """
        The provided SporkClass `ClassWithoutPublicConstructor` `<init>` does not have a public constructor.
        """.strip()

  @Test
  def testSporkClassWithParameterError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ShouldError.ClassWithParameters(10).pack()
        """
      .contains:
        """
        The constructor of the provided SporkClass `ClassWithParameters` `<init>` does not have an empty parameter list.
        """.strip()

  @Test
  def testSporkClassWithContextParameterError(): Unit =
    // Catches a common mistake in which implicit parameters are used in the 
    // constructor. For example, this would seem like a reasonable thing to do, 
    // but will not work:
    // 
    // class PackedRW[T: ReadWriter] extends SporkClass[ReadWriter[T]](summon[ReadWriter[T]])
    // given PackedSpork[ReadWriter[T]] = PackedRW[T].pack()
    // 
    // // This will crash at runtime, as the init method is assumed to not have any params.
    // summon[PackedSpork[ReadWriter[Int]]].build() 

    assertTrue:
      typeCheckErrors:
        """
        given ShouldError.F[Int] = new ShouldError.F[Int]()
        ShouldError.ClassWithContex[Int].pack()
        """
      .contains:
        """
        The constructor of the provided SporkClass `ClassWithContex` `<init>` contains a context parameter list.
        """.strip()
