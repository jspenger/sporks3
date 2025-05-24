package sporks


private[sporks] object Reflect {

  import scala.annotation.StaticAnnotation
  // Dummy annotation as JVM reflection is enabled by default
  class EnableReflectiveInstantiation extends StaticAnnotation

  def getModuleFieldValue[T](name: String): T =
    Class.forName(name).getDeclaredField("MODULE$").get(null).asInstanceOf[T]

  def getClassInstance[T](name: String): T =
    Class.forName(name).getDeclaredConstructor().newInstance().asInstanceOf[T]
}
