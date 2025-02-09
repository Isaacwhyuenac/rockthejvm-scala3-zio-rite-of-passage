package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

class ReviewController private (reviewService: ReviewService) extends BaseController with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { (req: CreateReviewRequest) =>
    reviewService.create(req, -1L).either
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { (id: Long) =>
    reviewService.getById(id).either
  }

  val getByCompanyId: ServerEndpoint[Any, Task] = getByCompanyIdEndpoint.serverLogic { (companyId: Long) =>
    reviewService.getByCompanyId(companyId).either
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId)

}

object ReviewController {

  val makeZIO: ZIO[ReviewService, Nothing, ReviewController] = for {
    reviewService <- ZIO.service[ReviewService]
  } yield new ReviewController(reviewService)

}
