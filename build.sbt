ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion = "2.0.19"
val tapirVersion = "1.2.6"
val zioLoggingVersion = "1.2.6"


val dependencies = Seq(
"com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % tapirVersion,
)





lazy val root = (project in file("."))
  .settings(
    name := "rockthejvm-scala3-zio-rite-of-passage"
  )
