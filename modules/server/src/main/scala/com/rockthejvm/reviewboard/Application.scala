package com.rockthejvm.reviewboard

import sttp.tapir.{endpoint, plainBody, stringToPath}
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.http.Server
import zio.{Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}

object Application extends ZIOAppDefault {

  val healthEndpoint: Full[Unit, Unit, Unit, Unit, String, Any, Task] = endpoint
    .tag("health")
    .name("health")
    .description("Health check endpoint")
    .get
    .in("health")
    .out(plainBody[String])
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  val serverProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default
    ).toHttp(List(healthEndpoint))
  )

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    serverProgram.provide(
      Server.default
    )

}
