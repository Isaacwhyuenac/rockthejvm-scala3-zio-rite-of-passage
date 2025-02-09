package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Review
import com.rockthejvm.reviewboard.http.requests.CreateReviewRequest
import com.rockthejvm.reviewboard.services.ReviewService
import com.rockthejvm.reviewboard.testdata.ReviewTestDataSpec
import sttp.client3.{basicRequest, SttpBackend, UriContext}
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.{Scope, Task, ZIO, ZLayer}
import zio.json.{DecoderOps, EncoderOps}
import zio.test.{assertTrue, assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}

object ReviewControllerSpec extends ZIOSpecDefault with ReviewTestDataSpec {

  // requirement for `backendStubZIO` to work
  private given zioMonad: sttp.monad.MonadError[zio.Task] = RIOMonadError[Any]

  private val serviceStub = new ReviewService {
    override def create(request: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(goodReview)

    override def getById(id: Long): Task[Option[Review]] =
      ZIO.succeed {
        id match {
          case goodReview.id => Some(goodReview)
          case _             => None
        }
      }

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
      ZIO.succeed {
        companyId match {
          case goodReview.companyId => List(goodReview)
          case _                    => List()
        }
      }

    override def getByUserId(userId: Long): Task[List[Review]] =
      ZIO.succeed {
        userId match {
          case goodReview.userId => List(goodReview)
          case _                 => List()
        }
      }
  }

  private def backendStubZIO(
      endpointFn: ReviewController => ServerEndpoint[Any, Task]
  ): ZIO[ReviewService, Nothing, SttpBackend[Task, Any]] =
    for {
      /// create the controller
      reviewController <- ReviewController.makeZIO
      /// build tapir backend
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointFn(reviewController))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ReviewControllerSpec")(
      test("post review") {
        val program = for {
          backendStub <- backendStubZIO(_.create)

          // run client request
          response <- basicRequest
            .post(uri"/reviews")
            .body(
              CreateReviewRequest(
                goodReview.companyId,
                goodReview.management,
                goodReview.culture,
                goodReview.salary,
                goodReview.benefits,
                goodReview.wouldRecommend,
                goodReview.review
              ).toJson
            )
            .send(backendStub)
        } yield response.body

        assertZIO(program)(
          Assertion.assertion("review are equal") { case respBody =>
            respBody.toOption
              .flatMap(_.fromJson[Review].toOption) // Option[Review]
              .contains(goodReview)
          }
        )
      },
      test("get review by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
            .get(uri"/reviews/${goodReview.id}")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"/reviews/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption.flatMap(_.fromJson[Review].toOption).contains(goodReview) &&
            responseNotFound.body.toOption.flatMap(_.fromJson[Review].toOption).isEmpty
        )
      },
      test("get review by companyId") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
            .get(uri"/reviews/company/${goodReview.companyId}")
            .send(backendStub)
          responseNotFound <- basicRequest
            .get(uri"/reviews/company/999")
            .send(backendStub)
        } yield assertTrue(
          response.body.toOption.flatMap(_.fromJson[List[Review]].toOption).contains(List(goodReview)) &&
            responseNotFound.body.toOption.flatMap(_.fromJson[List[Review]].toOption).isEmpty
        )
      }
    ).provide(ZLayer.succeed(serviceStub))
}
