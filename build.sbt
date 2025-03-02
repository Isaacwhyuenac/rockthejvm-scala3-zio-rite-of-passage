ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.6.3"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature"
)

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion        = "2.1.14"
val tapirVersion      = "1.11.13"
val zioLoggingVersion = "2.4.0"
val zioConfigVersion  = "4.0.3"
val sttpVersion       = "3.10.2"
val javaMailVersion   = "1.6.2"
val stripeVersion     = "28.3.0"

val commonDependencies = Seq(
  "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client" % tapirVersion,
  "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % tapirVersion,
  "com.softwaremill.sttp.client3" %% "zio"               % sttpVersion
)

val serverDependencies = commonDependencies ++ Seq(
  "dev.zio"                     %% "zio-json"                          % "0.7.8",
  "com.softwaremill.sttp.tapir" %% "tapir-zio"                         % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"             % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"           % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"            % tapirVersion % "test",
  "dev.zio"                     %% "zio-logging"                       % zioLoggingVersion,
  "dev.zio"                     %% "zio-logging-slf4j"                 % zioLoggingVersion,
  "ch.qos.logback"               % "logback-classic"                   % "1.5.16",
  "dev.zio"                     %% "zio-test"                          % zioVersion,
  "dev.zio"                     %% "zio-test-junit"                    % zioVersion   % "test",
  "dev.zio"                     %% "zio-test-sbt"                      % zioVersion   % "test",
  "dev.zio"                     %% "zio-test-magnolia"                 % zioVersion   % "test",
  "dev.zio"                     %% "zio-mock"                          % "1.0.0-RC12" % "test",
  "dev.zio"                     %% "zio-config"                        % zioConfigVersion,
  "dev.zio"                     %% "zio-config-magnolia"               % zioConfigVersion,
  "dev.zio"                     %% "zio-config-typesafe"               % zioConfigVersion,
  "io.getquill"                 %% "quill-jdbc-zio"                    % "4.8.6",
  "org.postgresql"               % "postgresql"                        % "42.7.5",
  "org.flywaydb"                 % "flyway-core"                       % "11.3.4",
  "io.github.scottweaver"       %% "zio-2-0-testcontainers-postgresql" % "0.10.0",
  "dev.zio"                     %% "zio-prelude"                       % "1.0.0-RC39",
  "com.auth0"                    % "java-jwt"                          % "4.5.0",
  "com.sun.mail"                 % "javax.mail"                        % javaMailVersion,
  "com.stripe"                   % "stripe-java"                       % stripeVersion
) ++ Dependencies.openTelemetry ++ Dependencies.zioMetrics

lazy val foundations = (project in file("modules/foundations"))
  .settings(
    libraryDependencies ++= serverDependencies
  )

lazy val common = (crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure) in file("modules/common"))
  .settings(
    libraryDependencies ++= commonDependencies
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0" // implementations of java.time classes for Scala.JS,
    )
  )

lazy val server = (project in file("modules/server"))
  .settings(
    libraryDependencies ++= serverDependencies
  )

lazy val app = (project in file("modules/app"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"                       %%% "zio-json"          % "0.4.2",
      "io.frontroute"                 %%% "frontroute"        % "0.18.1" // Brings in Laminar 16
    ) ++ commonDependencies,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    semanticdbEnabled               := true,
    autoAPIMappings                 := true,
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass             := Some("com.rockthejvm.reviewboard.App")
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(common.js)

lazy val root = (project in file("."))
  .settings(
    name := "zio-rite-of-passage"
  )
  .aggregate(app, server)
  .dependsOn(app, server)
