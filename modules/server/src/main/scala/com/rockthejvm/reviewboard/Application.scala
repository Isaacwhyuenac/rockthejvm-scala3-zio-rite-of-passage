package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.{CompanyRepositoryLive, Repository, ReviewRepositoryLive}
import com.rockthejvm.reviewboard.services.{CompanyServiceLive, ReviewServiceLive}
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import io.opentelemetry.api
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.http.Server
import zio.metrics.connectors.micrometer
import zio.metrics.connectors.micrometer.MicrometerConfig
import zio.metrics.jvm.DefaultJvmMetrics
import zio.telemetry.opentelemetry
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Application extends ZIOAppDefault {

  private val instrumentationScopeName = "zio.telemetry.opentelemetry.instrumentation.example.ServerApp"

  val serverProgram = for {
    endpoints     <- HttpApi.endpointsZIO
    openTelemetry <- ZIO.service[api.OpenTelemetry]
    _             <- ZIO.attempt(OpenTelemetryAppender.install(openTelemetry))
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(endpoints)
    )

    _ <- zio.Console.printLine("Rock the JVM!")
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    serverProgram.provide(
      Server.default,
      Repository.dataLayer,
      // services
      CompanyServiceLive.layer,
      ReviewServiceLive.layer,

      // repositories
      CompanyRepositoryLive.layer,
      ReviewRepositoryLive.layer,

      // OTEL
      opentelemetry.OpenTelemetry.global,
//      opentelemetry.OpenTelemetry.tracing(instrumentationScopeName),
//      opentelemetry.OpenTelemetry.contextJVM,

      // Metrics
      ZLayer.succeed(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)),
      ZLayer.succeed(MicrometerConfig.default),
      micrometer.micrometerLayer,
      zio.Runtime.enableRuntimeMetrics,
      DefaultJvmMetrics.live.unit,
    )

}
