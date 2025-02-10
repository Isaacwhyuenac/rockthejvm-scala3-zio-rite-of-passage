package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.{
  BaseController,
  CompanyController,
  HealthController,
  ReviewController,
  UserController
}
import sttp.tapir.server.ServerEndpoint
import zio.Task

object HttpApi {
  private def gatherRoute(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  private def makeControllers = for {
    health  <- HealthController.makeZIO
    company <- CompanyController.makeZIO
    review  <- ReviewController.makeZIO
    user    <- UserController.makeZIO
  } yield List(health, company, review, user)

  val endpointsZIO = makeControllers.map(gatherRoute)
}
