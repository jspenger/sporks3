package sporks

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import sporks.given
import sporks.*
import sporks.TestUtils.*

object SporkClassBuilderErrorTests:
  object NotClzClz extends SporkClassBuilder[Int => Int](x => x)

  def someMethod: SporkClassBuilder[Int => Int] = {
    class Local extends SporkClassBuilder[Int => Int](x => x)
    new Local()
  }

  class NestedBuilderInClass:
    class Inner extends SporkClassBuilder[Int](10)

  class ClassWithoutPublicConstructor private () extends SporkClassBuilder[Int => Int](x => x)
  object ClassWithoutPublicConstructor:
    def apply(): ClassWithoutPublicConstructor = new ClassWithoutPublicConstructor()

  class ClassWithParameters(i: Int) extends SporkClassBuilder[() => Int](() => i)

  class F[T]
  class ClassWithContext1[T: F] extends SporkClassBuilder[F[T]](summon)
  class ClassWithContext2[T](using F[T]) extends SporkClassBuilder[F[T]](summon)
  class ClassWithContext3[T](implicit f: F[T]) extends SporkClassBuilder[F[T]](summon)

@RunWith(classOf[JUnit4])
class SporkClassBuilderErrorTests:
  import SporkClassBuilderErrorTests.*

  @Test
  def testObjectSporkClassBuilderError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        NotClzClz.pack()
        """
      .contains:
        """
        The provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.NotClzClz$` is not a class.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        val notClzClz = NotClzClz
        notClzClz.pack()
        """
      .contains:
        """
        The provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.NotClzClz$` is not a class.
        """.trim()

  @Test
  def testSporkClassBuilderNestedInClassError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        val builder = new NestedBuilderInClass()
        val pred = new builder.Inner()
        pred.pack()
        """
      .contains:
        """
        The provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.NestedBuilderInClass.Inner` is nested in a class.
        """.trim()

  @Test
  def testSporkClassBuilderNestedInMethodError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        someMethod.pack()
        """
      .contains:
        """
        The provided SporkClassBuilder `sporks.SporkClassBuilder` is not a concrete class.
        """.trim()

  @Test
  def testSporkClassBuilderWithPrivateConstructorError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ClassWithoutPublicConstructor().pack()
        """
      .contains:
        """
        The provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.ClassWithoutPublicConstructor` `<init>` does not have a public constructor.
        """.trim()

  @Test
  def testSporkClassBuilderWithParameterError(): Unit =
    assertTrue:
      typeCheckErrors:
        """
        ClassWithParameters(10).pack()
        """
      .contains:
        """
        The constructor of the provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.ClassWithParameters` `<init>` does not have an empty parameter list.
        """.trim()

  @Test
  def testSporkClassBuilderWithContextParameterError(): Unit =
    // Catches a common mistake in which implicit parameters are used in the
    // constructor. For example, this would seem like a reasonable thing to do,
    // but will not work:
    //
    // class PackedRW[T: ReadWriter] extends SporkClassBuilder[ReadWriter[T]](summon[ReadWriter[T]])
    // given Spork[ReadWriter[T]] = PackedRW[T].pack()
    //
    // // This will crash at runtime, as the init method is assumed to not have any params.
    // summon[Spork[ReadWriter[Int]]].unwrap()

    assertTrue:
      typeCheckErrors:
        """
        given F[Int] = new F[Int]()
        ClassWithContext1[Int].pack()
        """
      .contains:
        """
        The constructor of the provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.ClassWithContext1` `<init>` contains a context parameter list.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        given F[Int] = new F[Int]()
        ClassWithContext2[Int].pack()
        """
      .contains:
        """
        The constructor of the provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.ClassWithContext2` `<init>` contains a context parameter list.
        """.trim()

    assertTrue:
      typeCheckErrors:
        """
        given F[Int] = new F[Int]()
        ClassWithContext3[Int].pack()
        """
      .contains:
        """
        The constructor of the provided SporkClassBuilder `sporks.SporkClassBuilderErrorTests$.ClassWithContext3` `<init>` contains a context parameter list.
        """.trim()
