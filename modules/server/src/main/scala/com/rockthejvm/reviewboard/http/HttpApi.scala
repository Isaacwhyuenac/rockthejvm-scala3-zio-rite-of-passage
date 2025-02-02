package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.{BaseController, CompanyController, HealthController}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

object HttpApi {
  private def gatherRoute(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers: ZIO[Any, Nothing, List[BaseController]] = for {
    health  <- HealthController.makeZIO
    company <- CompanyController.makeZIO
  } yield List(health, company)

  val endpointsZIO: ZIO[Any, Nothing, List[ServerEndpoint[Any, Task]]] = makeControllers.map(gatherRoute)
}
