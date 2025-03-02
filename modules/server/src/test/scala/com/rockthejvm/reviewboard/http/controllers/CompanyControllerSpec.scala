package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.{Company, User, UserID, UserToken}
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.services.{CompanyService, JWTService}
import com.rockthejvm.reviewboard.syntax.assert
import com.rockthejvm.reviewboard.testdata.CompanyTestDataSpec
import sttp.client3.{basicRequest, SttpBackend, UriContext}
import zio.{Scope, Task, ZIO, ZLayer}
import zio.json.{DecoderOps, EncoderOps}
import zio.test.{assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.RIOMonadError

object CompanyControllerSpec extends ZIOSpecDefault with CompanyTestDataSpec {

  // requirement for `backendStubZIO` to work
  private given zioMonad: sttp.monad.MonadError[zio.Task] = RIOMonadError[Any]

  private val serviceStub = new CompanyService {
    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(rockthejvm)

    override def getAll(): Task[List[Company]] = ZIO.succeed(List(rockthejvm))

    override def getById(id: Long): Task[Option[Company]] = ZIO.succeed {
      if (id == rockthejvm.id) Some(rockthejvm)
      else None
    }

    override def getBySlug(slug: String): Task[Option[Company]] = ZIO.succeed {
      if (slug == rockthejvm.slug) Some(rockthejvm)
      else None
    }
  }

  private val jwtServiceStub = new JWTService {
    override def createToken(user: User): Task[UserToken] =
      ZIO.succeed(UserToken(user.email, "ALL_IS_GOOD", Long.MaxValue))

    override def verifyToken(token: String): Task[UserID] = ZIO.succeed(UserID(1, "daniel@rockthejvm.com"))
  }

  private def backendStubZIO(
      endpointFn: CompanyController => ServerEndpoint[Any, Task]
  ) =
    for {
      /// create the controller
      companyController <- CompanyController.makeZIO
      /// build tapir backend
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointFn(companyController))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("CompanyControllerSpec")(
    test("post company") {
      val program = for {
        backendStub <- backendStubZIO(_.create)

        // run client request
        response <- basicRequest
          .post(uri"/companies")
          .header("Authorization", "Bearer ALL_IS_GOOD")
          .body(CreateCompanyRequest("Rock the JVM", "rockthejvm.com").toJson)
          .send(backendStub)
      } yield response.body

      program.assert { respBody =>
        respBody.toOption
          .flatMap(_.fromJson[Company].toOption) // Option[Company]
          .contains(Company(1, "rock-the-jvm", "Rock the JVM", "rockthejvm.com"))
      }

    },
    test("get all") {
      // controller
      // stub server
      // run request
      // inspect response

      val program = for {
        backendStub <- backendStubZIO(_.getAll)
        // run client request
        response <- basicRequest
          .get(uri"/companies")
          .send(backendStub)
      } yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from getAll") { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[List[Company]].toOption) // Option[List[Company]]
            .contains(List(rockthejvm))
        }
      )
    },
    test("get by id") {
      // controller
      // stub server
      // run request
      // inspect response

      val program = for {
        backendStub <- backendStubZIO(_.getById)

        // run client request
        response <- basicRequest
          .get(uri"/companies/1")
          .send(backendStub)
      } yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from getAll") { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption) // Option[Company]
            .contains(rockthejvm)
        }
      )
    }
  )
    .provide(ZLayer.succeed(serviceStub), ZLayer.succeed(jwtServiceStub))

}
