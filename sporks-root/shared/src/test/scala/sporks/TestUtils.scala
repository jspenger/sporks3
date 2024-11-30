package sporks

object TestUtils {

  inline def typeCheckSuccess(inline str: String): Boolean =
    scala.compiletime.testing.typeChecks(str)

  inline def typeCheckFail(inline str: String): Boolean =
    !scala.compiletime.testing.typeChecks(str)

  inline def typeCheckErrors(inline str: String): List[String] =
    scala.compiletime.testing.typeCheckErrors(str).map(_.message)
}
