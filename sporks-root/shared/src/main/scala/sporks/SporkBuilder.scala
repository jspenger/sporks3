package sporks

import upickle.default.*
import scala.quoted.*

import sporks.Reflect
import sporks.Spork.*
import sporks.PackedSpork.*

@Reflect.EnableReflectiveInstantiation
trait SporkObjectBuilder[+T](val fun: T) {

  final inline def pack(): PackedObject[T] =
    ${ SporkObjectBuilder.packMacro('this) }

  final inline def build(): SporkObject[T] =
    ${ SporkObjectBuilder.buildMacro('this) }
}


@Reflect.EnableReflectiveInstantiation
trait SporkClassBuilder[+T](val fun: T) {

  final inline def pack(): PackedClass[T] =
    ${ SporkClassBuilder.packMacro('this) }

  final inline def build(): SporkClass[T] =
    ${ SporkClassBuilder.buildMacro('this) }
}


@Reflect.EnableReflectiveInstantiation
private[sporks] trait SporkLambdaBuilder[+T](val fun: T) {

  final inline def pack(): PackedLambda[T] =
    ${ SporkLambdaBuilder.packMacro('this) }

  final inline def build(): SporkLambda[T] =
    ${ SporkLambdaBuilder.buildMacro('this) }
}


object SporkEnvBuilder {
  def apply[T](env: T)(using rw: PackedSpork[ReadWriter[T]]): PackedEnv[T] =
    this.pack(env)(using rw)

  def pack[T](env: T)(using rw: PackedSpork[ReadWriter[T]]): PackedEnv[T] =
    PackedEnv(write(env)(using rw.unwrap()), rw)

  def build[T](env: T)(using rw: Spork[ReadWriter[T]]): SporkEnv[T] =
    SporkEnv(env, rw)
}


private[sporks] object SporkObjectBuilder {

  def toString[T](builder: SporkObjectBuilder[T]): String =
    builder.getClass().getName()

  def fromString[T](str: String): SporkObjectBuilder[T] =
    Reflect.getModuleFieldValue[SporkObjectBuilder[T]](str)

  def packMacro[T](objectExpr: Expr[SporkObjectBuilder[T]])(using Type[T], Quotes): Expr[PackedObject[T]] =
    Macros.isTopLevelObject(objectExpr)
    '{ PackedObject($objectExpr.getClass().getName()) }

  def buildMacro[T](objectExpr: Expr[SporkObjectBuilder[T]])(using Type[T], Quotes): Expr[SporkObject[T]] =
    Macros.isTopLevelObject(objectExpr)
    '{ SporkObject($objectExpr) }
}


private[sporks] object SporkClassBuilder {

  def toString[T](builder: SporkClassBuilder[T]): String =
    builder.getClass().getName()

  def fromString[T](str: String): SporkClassBuilder[T] =
    Reflect.getClassInstance[SporkClassBuilder[T]](str)

  def packMacro[T](classExpr: Expr[SporkClassBuilder[T]])(using Type[T], Quotes): Expr[PackedClass[T]] =
    Macros.isTopLevelClass(classExpr)
    '{ PackedClass($classExpr.getClass().getName()) }

  def buildMacro[T](classExpr: Expr[SporkClassBuilder[T]])(using Type[T], Quotes): Expr[SporkClass[T]] =
    Macros.isTopLevelClass(classExpr)
    '{ SporkClass($classExpr) }
}


private[sporks] object SporkLambdaBuilder {

  def toString[T](builder: SporkLambdaBuilder[T]): String =
    builder.getClass().getName()

  def fromString[T](str: String): SporkLambdaBuilder[T] =
    Reflect.getClassInstance[SporkLambdaBuilder[T]](str)

  def packMacro[T](lambdaExpr: Expr[SporkLambdaBuilder[T]])(using Type[T], Quotes): Expr[PackedLambda[T]] =
    // No checks needed, all relevant checks are done in the lambda factory `SporkBuilder.apply`.
    '{ PackedLambda($lambdaExpr.getClass().getName()) }

  def buildMacro[T](lambdaExpr: Expr[SporkLambdaBuilder[T]])(using Type[T], Quotes): Expr[SporkLambda[T]] =
    // No checks needed, all relevant checks are done in the lambda factory `SporkBuilder.apply`.
    '{ SporkLambda($lambdaExpr) }
}
