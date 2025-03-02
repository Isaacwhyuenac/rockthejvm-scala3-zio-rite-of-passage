package com.rockthejvm.reviewboard.http.requests

import zio.json.{DeriveJsonCodec, JsonCodec}

case class CreateReviewRequest(
    companyId: Long,
    // score
    management: Int, // 1-5
    culture: Int,
    salary: Int,
    benefits: Int,
    wouldRecommend: Int,
    review: String
)

object CreateReviewRequest {
  given codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
}
