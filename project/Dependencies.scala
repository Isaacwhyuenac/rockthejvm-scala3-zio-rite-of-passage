import sbt._

object Dependencies {

  val zioTelemetryVersion  = "3.1.1"
  val opentelemetryVersion = "1.47.0"
  val micrometerVersion    = "2.3.1"

  val zio = Seq(
  )

  val zioJson = Seq(

  )

  val tapir = Seq(

  )

  val openTelemetry = Seq(
    // Zio Telemetry
    "dev.zio" %% "zio-opentelemetry"             % zioTelemetryVersion,
    "dev.zio" %% "zio-opentelemetry-zio-logging" % zioTelemetryVersion,

    // OpenTelemetry
    "io.opentelemetry"                 % "opentelemetry-sdk"                  % opentelemetryVersion,
    "io.opentelemetry"                 % "opentelemetry-exporter-otlp"        % opentelemetryVersion,
    "io.opentelemetry"                 % "opentelemetry-sdk"                  % opentelemetryVersion,
    "io.opentelemetry.semconv"         % "opentelemetry-semconv"              % "1.30.0-rc.1",
    "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % "2.12.0-alpha",
    "io.grpc"                          % "grpc-netty-shaded"                  % "1.70.0"
  )

  val zioMetrics = Seq(
    "dev.zio"      %% "zio-metrics-connectors"            % micrometerVersion,
    "dev.zio"      %% "zio-metrics-connectors-prometheus" % micrometerVersion,
    "io.micrometer" % "micrometer-registry-prometheus"    % "1.14.3",
    "dev.zio"      %% "zio-metrics-connectors-micrometer" % micrometerVersion
  )

}
