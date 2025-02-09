package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.http.endpoints.ReviewEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.{CompanyService, ReviewService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

class ReviewController private (reviewService: ReviewService) extends BaseController with ReviewEndpoints {

  val create = createEndpoint.serverLogicSuccess { (req: CreateReviewRequest) =>
    reviewService.create(req, -1L)
  }

  val getById = getByIdEndpoint.serverLogicSuccess { (id: Long) =>
    reviewService.getById(id)
  }

  val getByCompanyId = getByCompanyIdEndpoint.serverLogicSuccess { (companyId: Long) =>
    reviewService.getByCompanyId(companyId)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId)
  
}

object ReviewController {

  val makeZIO = for {
    reviewService <- ZIO.service[ReviewService]
  } yield new ReviewController(reviewService)

}

