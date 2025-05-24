package sporks


private[sporks] object Reflect {

  import scala.scalajs.reflect.{Reflect => JSReflect}

  export scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

  def getModuleFieldValue[T](name: String): T =
    JSReflect.lookupLoadableModuleClass(name).get.loadModule().asInstanceOf[T]

  def getClassInstance[T](name: String): T =
    JSReflect.lookupInstantiatableClass(name).get.newInstance().asInstanceOf[T]
}
