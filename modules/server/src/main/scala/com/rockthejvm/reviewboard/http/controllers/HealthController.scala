package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio.{Task, ZIO}

class HealthController private extends BaseController with  HealthEndpoint {
  val health = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  private val errorRoute = errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Error!")).either) // ZIO[R, Nothing, Either[Throwable, A]]

  override val routes: List[ServerEndpoint[Any, Task]] = List(health, errorRoute)
}

object HealthController {
  val makeZIO: ZIO[Any, Nothing, HealthController] = ZIO.succeed(new HealthController)
}