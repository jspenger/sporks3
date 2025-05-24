package sporks


private[sporks] object Reflect {

  import scala.scalanative.reflect.{Reflect => NativeReflect}

  export scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

  def getModuleFieldValue[T](name: String): T =
    NativeReflect.lookupLoadableModuleClass(name).get.loadModule().asInstanceOf[T]

  def getClassInstance[T](name: String): T =
    NativeReflect.lookupInstantiatableClass(name).get.newInstance().asInstanceOf[T]
}
