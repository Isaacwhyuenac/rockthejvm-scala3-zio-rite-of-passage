package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import com.rockthejvm.reviewboard.testdata.ReviewTestDataSpec
import zio.{Scope, Task, ZIO, ZLayer}
import zio.test.{assertTrue, Spec, TestEnvironment, ZIOSpecDefault}

object ReviewServiceTestDataSpec extends ZIOSpecDefault with ReviewTestDataSpec {

  val stubRepo: ZLayer[Any, Nothing, ReviewRepository] = ZLayer.succeed {
    new ReviewRepository {
      override def create(review: Review): Task[Review] =
        ZIO.succeed(goodReview)

      override def getById(id: Long): Task[Option[Review]] =
        ZIO.succeed {
          id match {
            case goodReview.id => Some(goodReview)
            case badReview.id  => Some(badReview)
            case _             => None
          }
        }

      override def getByCompanyId(companyId: Long): Task[List[Review]] =
        ZIO.succeed {
          companyId match {
            case goodReview.companyId => List(goodReview, badReview)
            case _                    => List()
          }
        }

      override def getByUserId(userId: Long): Task[List[Review]] =
        ZIO.succeed {
          userId match {
            case goodReview.userId => List(goodReview, badReview)
            case _                 => List()
          }
        }

      override def update(id: Long, ops: Review => Review): Task[Review] =
        for {
          review <- getById(id).someOrFail(new RuntimeException(s"id $id not found"))
          _      <- ZIO.succeed(ops(review))
        } yield review

      override def delete(id: Long): Task[Option[Review]] =
        for {
          review <- getById(id).someOrFail(new RuntimeException(s"id $id not found"))
        } yield Some(review)
    }
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewServiceTestDataSpec")(
      test("create") {
        for {
          service <- ZIO.service[ReviewService]
          review <- service.create(
            CreateReviewRequest(
              companyId = goodReview.companyId,
              management = goodReview.management,
              culture = goodReview.culture,
              salary = goodReview.salary,
              benefits = goodReview.benefits,
              wouldRecommend = goodReview.wouldRecommend,
              review = goodReview.review
            ),
            goodReview.userId
          )
        } yield assertTrue(
          review.companyId == goodReview.companyId &&
            review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
        )
      },
      test("get review by id") {
        for {
          service        <- ZIO.service[ReviewService]
          review         <- service.getById(goodReview.id)
          reviewNotFound <- service.getById(999L)
        } yield assertTrue(
          review.contains(goodReview) &&
            reviewNotFound.isEmpty
        )
      },
      test("get review by company id") {
        for {
          service         <- ZIO.service[ReviewService]
          reviews         <- service.getByCompanyId(goodReview.companyId)
          reviewsNotFound <- service.getByCompanyId(999L)
        } yield assertTrue(
          reviews.toSet == Set(goodReview, badReview) &&
            reviewsNotFound.isEmpty
        )
      },
      test("get review by user id") {
        for {
          service         <- ZIO.service[ReviewService]
          reviews         <- service.getByUserId(goodReview.userId)
          reviewsNotFound <- service.getByUserId(999L)
        } yield assertTrue(
          reviews.toSet == Set(goodReview, badReview) &&
            reviewsNotFound.isEmpty
        )
      }
    ).provide(ReviewServiceLive.layer, stubRepo)

}
