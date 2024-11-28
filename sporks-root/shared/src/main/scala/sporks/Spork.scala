package sporks

import sporks.Reflect
import upickle.default.*
import scala.quoted.*

////////////////////////////////////////////////////////////////////////////////
// Spork builders
////////////////////////////////////////////////////////////////////////////////

@Reflect.EnableReflectiveInstantiation
trait SporkObject[+T](val fun: T)

@Reflect.EnableReflectiveInstantiation
trait SporkClass[+T] (val fun: T)

@Reflect.EnableReflectiveInstantiation
private[sporks] trait SporkLambda[+T](val fun: T)

////////////////////////////////////////////////////////////////////////////////
// PackedSpork
////////////////////////////////////////////////////////////////////////////////

sealed trait PackedSpork[+T]
case class PackedObject[+T](fun: String) extends PackedSpork[T] 
case class PackedClass[+T] (fun: String) extends PackedSpork[T] 
case class PackedLambda[+T](fun: String) extends PackedSpork[T]
case class PackedWithEnv[E, +T](packed: PackedSpork[E => T],  env: String, envRW: PackedSpork[ReadWriter[E]]) extends PackedSpork[T]
case class PackedWithCtx[E, +T](packed: PackedSpork[E ?=> T], env: String, envRW: PackedSpork[ReadWriter[E]]) extends PackedSpork[T]

////////////////////////////////////////////////////////////////////////////////
// pack(); packWithEnv(); packWithCtx()
////////////////////////////////////////////////////////////////////////////////

extension [T](inline builder: SporkObject[T]){ inline def pack(): PackedObject[T] = ${ packMacroObject('builder) } }
extension [T](inline builder: SporkClass[T]) { inline def pack(): PackedClass[T] =  ${ packMacroClass('builder) } }
extension [T](inline builder: SporkLambda[T]){ inline def pack(): PackedLambda[T] = ${ packMacroLambda('builder) } }


private def packMacroObject[T](builderExpr: Expr[SporkObject[T]])(using Type[T], Quotes): Expr[PackedObject[T]] =
  '{ PackedObject($builderExpr.getClass().getName()) }

private def packMacroLambda[T](lambdaExpr: Expr[SporkLambda[T]])(using Type[T], Quotes): Expr[PackedLambda[T]] =
  '{ PackedLambda($lambdaExpr.getClass().getName()) }

private def packMacroClass[T](classExpr: Expr[SporkClass[T]])(using Type[T], Quotes): Expr[PackedClass[T]] =
  '{ PackedClass($classExpr.getClass().getName()) }


extension [T, R](inline packed: PackedSpork[T => R]) {
  inline def packWithEnv(inline env: T)(using prw: PackedSpork[ReadWriter[T]]): PackedWithEnv[T, R] = 
    PackedWithEnv(packed, write(env)(using prw.build()), prw)
}

extension [T, R](inline packed: PackedSpork[T ?=> R]) {
  inline def packWithCtx(inline env: T)(using prw: PackedSpork[ReadWriter[T]]): PackedWithCtx[T, R] = 
    PackedWithCtx(packed, write(env)(using prw.build()), prw)
}

////////////////////////////////////////////////////////////////////////////////
// build()
////////////////////////////////////////////////////////////////////////////////

extension [T](packed: PackedSpork[T]){ 
  def build(): T = 
    def unpackEnv[E](env: String, rw: PackedSpork[ReadWriter[E]]): E = read[E](env)(using rw.build())
    packed match
      case PackedObject(fun) => 
        Reflect.getModuleFieldValue[SporkObject[T]](fun).fun
      case PackedClass(fun) =>
        Reflect.getClassInstance[SporkClass[T]](fun).fun
      case PackedLambda(fun) => 
        Reflect.getClassInstance[SporkLambda[T]](fun).fun
      case PackedWithEnv(packed, env, envRW) =>
        packed.build()(unpackEnv(env, envRW))
      case PackedWithCtx(packed, env, envRW) =>
        packed.build()(using unpackEnv(env, envRW))
}
