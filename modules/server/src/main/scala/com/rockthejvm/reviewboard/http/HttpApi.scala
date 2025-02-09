package com.rockthejvm.reviewboard.http

import com.rockthejvm.reviewboard.http.controllers.{BaseController, CompanyController, HealthController, ReviewController}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

object HttpApi {
  private def gatherRoute(controllers: List[BaseController]): List[ServerEndpoint[Any, Task]] =
    controllers.flatMap(_.routes)

  def makeControllers = for {
    health  <- HealthController.makeZIO
    company <- CompanyController.makeZIO
    review <- ReviewController.makeZIO
  } yield List(health, company, review)

  val endpointsZIO = makeControllers.map(gatherRoute)
}
