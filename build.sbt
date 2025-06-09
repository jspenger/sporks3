lazy val scala33Version = "3.3.6"
lazy val upickleVersion = "3.1.0"
lazy val junitInterfaceVersion = "0.11"


ThisBuild / scalaVersion := scala33Version
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.jspenger"
ThisBuild / licenses := List(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / developers := List(
  Developer(
    id    = "jspenger",
    name  = "Jonas Spenger",
    email = "@jonasspenger",
    url   = url("https://github.com/jspenger")
  )
)


lazy val root = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("sporks-root"))
  .settings(
    name := "sporks3",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
    libraryDependencies += "com.novocode" % "junit-interface" % junitInterfaceVersion % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeConfigure(_.enablePlugins(ScalaNativeJUnitPlugin))


lazy val example = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("sporks-example"))
  .settings(
    name := "sporks3-example",
    libraryDependencies += "com.lihaoyi" %%% "upickle" % upickleVersion,
    publish / skip := true
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true,
  )
  .dependsOn(root)
