package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import sttp.tapir.{endpoint, path, stringToPath, Endpoint}
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass

trait ReviewEndpoints {
  // post /reviews { CreateReviewRequest } => create review
  // return Review

  val createEndpoint: Endpoint[Unit, CreateReviewRequest, Unit, Review, Any] = endpoint
    .tag("Reviews")
    .name("create")
    .description("Create a review")
    .in("reviews")
    .post
    .in(jsonBody[CreateReviewRequest])
    .out(jsonBody[Review])

  // get /reviews/:id => get review by ID
  // return Option[Review]
  val getByIdEndpoint: Endpoint[Unit, Long, Unit, Option[Review], Any] = endpoint
    .tag("Reviews")
    .name("getById")
    .description("Get a review by ID")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  // get /reviews/company/:companyId => get reviews by company ID
  // return List[Review]
  val getByCompanyIdEndpoint: Endpoint[Unit, Long, Unit, List[Review], Any] = endpoint
    .tag("Reviews")
    .name("getByCompanyId")
    .description("Get reviews by company ID")
    .in("reviews" / "company" / path[Long]("companyId"))
    .get
    .out(jsonBody[List[Review]])

}
