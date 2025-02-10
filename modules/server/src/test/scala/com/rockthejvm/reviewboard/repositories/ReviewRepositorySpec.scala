package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.repositories.CompanyRepositorySpec.dataSourceLayer
import com.rockthejvm.reviewboard.testdata.ReviewTestDataSpec
import zio.test.{assertTrue, assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}
import zio.{Scope, ZIO}

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec with ReviewTestDataSpec {

  override val sqlScript: String = "sql/reviews.sql"
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewRepositorySpec")(
      test("create view") {
        val program = for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        } yield review

        assertZIO(program)(
          Assertion.assertion("review are equal") { case review =>
            review.management == goodReview.management &&
            review.culture == goodReview.culture &&
            review.salary == goodReview.salary &&
            review.benefits == goodReview.benefits &&
            review.wouldRecommend == goodReview.wouldRecommend &&
            review.review == goodReview.review
          }
        )
      },
      test("get review by ids (id, companyId, userId)") {
        for {
          repo           <- ZIO.service[ReviewRepository]
          review         <- repo.create(goodReview)
          fetchedReview  <- repo.getById(review.id)
          fetchedReview2 <- repo.getByCompanyId(review.companyId)
          fetchedReview3 <- repo.getByUserId(review.userId)
        } yield assertTrue(
          fetchedReview.contains(review) &&
            fetchedReview2.contains(review) &&
            fetchedReview3.contains(review)
        )
      },
      test("get all") {
        for {
          repo           <- ZIO.service[ReviewRepository]
          review1        <- repo.create(goodReview)
          review2        <- repo.create(badReview)
          reviewsCompany <- repo.getByCompanyId(goodReview.companyId)
          reviewsUser    <- repo.getByUserId(goodReview.userId)
        } yield assertTrue(
          reviewsCompany.toSet == Set(review1, review2) &&
            reviewsUser.toSet == Set(review1, review2)
        )
      },
      test("edit review") {
        for {
          repo     <- ZIO.service[ReviewRepository]
          original <- repo.create(goodReview)
          value = "not too bad"
          updated <- repo.update(original.id, _.copy(review = value))
        } yield assertTrue(
          updated.id == original.id &&
            updated.companyId == original.companyId &&
            updated.userId == original.userId &&
            updated.management == original.management &&
            updated.culture == original.culture &&
            updated.salary == original.salary &&
            updated.benefits == original.benefits &&
            updated.wouldRecommend == original.wouldRecommend &&
            updated.review == value &&
            updated.created == original.created
//            updated.updated != original.updated
        )
      },
      test("delete review") {
        for {
          repo        <- ZIO.service[ReviewRepository]
          review      <- repo.create(goodReview)
          _           <- repo.delete(review.id)
          maybeReview <- repo.getById(review.id)
        } yield assertTrue(
          maybeReview.isEmpty
        )
      }
    ).provide(
      ReviewRepositoryLive.layer,
      Repository.quillLayer,
      dataSourceLayer,
      Scope.default
    )

}
