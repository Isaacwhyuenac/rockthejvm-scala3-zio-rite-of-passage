package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.ServerEndpoint.Full
import zio.{Task, ZIO}

class ReviewController private (reviewService: ReviewService) extends BaseController with ReviewEndpoints {

  val create: Full[Unit, Unit, CreateReviewRequest, Unit, Review, Any, Task] = createEndpoint.serverLogicSuccess { (req: CreateReviewRequest) =>
    reviewService.create(req, -1L)
  }

  val getById: Full[Unit, Unit, Long, Unit, Option[Review], Any, Task] = getByIdEndpoint.serverLogicSuccess { (id: Long) =>
    reviewService.getById(id)
  }

  val getByCompanyId: Full[Unit, Unit, Long, Unit, List[Review], Any, Task] = getByCompanyIdEndpoint.serverLogicSuccess { (companyId: Long) =>
    reviewService.getByCompanyId(companyId)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId)

}

object ReviewController {

  val makeZIO: ZIO[ReviewService, Nothing, ReviewController] = for {
    reviewService <- ZIO.service[ReviewService]
  } yield new ReviewController(reviewService)

}
