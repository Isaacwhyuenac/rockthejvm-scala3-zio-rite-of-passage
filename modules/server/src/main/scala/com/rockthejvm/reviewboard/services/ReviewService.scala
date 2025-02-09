package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.repositories.ReviewRepository
import zio.{Task, ZIO, ZLayer}

import java.time.Instant

trait ReviewService {
  def create(request: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService {
  override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
    for {
      createdReview <- repo.create(
        Review(
          -1L,
          request.companyId,
          userId,
          request.management,
          request.culture,
          request.salary,
          request.benefits,
          request.wouldRecommend,
          request.review,
          Instant.now(),
          Instant.now()
        )
      )
    } yield createdReview

  override def getById(id: Long): Task[Option[Review]] =
    repo.getById(id)

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    repo.getByCompanyId(companyId)

  override def getByUserId(userId: Long): Task[List[Review]] =
    repo.getByUserId(userId)
}

object ReviewServiceLive {
  val layer: ZLayer[ReviewRepository, Nothing, ReviewService] = ZLayer {
    for {
      repo <- ZIO.service[ReviewRepository]
    } yield new ReviewServiceLive(repo)
  }
}
