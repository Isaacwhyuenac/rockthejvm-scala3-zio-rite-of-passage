package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.syntax.assert
import sttp.client3.{basicRequest, UriContext}
import zio.{Scope, Task, ZIO}
import zio.json.{DecoderOps, EncoderOps}
import zio.test.{assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.RIOMonadError

object CompanyControllerSpec extends ZIOSpecDefault {

  private given zioMonad: sttp.monad.MonadError[zio.Task] = RIOMonadError[Any]

  private def backendStubZIO(endpointFn: CompanyController => ServerEndpoint[Any, Task]) =
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
      val program: ZIO[Any, Throwable, Either[String, String]] = for {
        backendStub <- backendStubZIO(_.create)

        // run client request
        response <- basicRequest
          .post(uri"/companies")
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

      val program: ZIO[Any, Throwable, Either[String, String]] = for {
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
            .contains(List.empty)
        }
      )
    },
    test("get by id") {
      // controller
      // stub server
      // run request
      // inspect response

      val program: ZIO[Any, Throwable, Either[String, String]] = for {
        backendStub <- backendStubZIO(_.getById)

        // run client request
        response <- basicRequest
          .get(uri"/company/1")
          .send(backendStub)
      } yield response.body

      assertZIO(program)(
        Assertion.assertion("inspect http response from getAll") { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption) // Option[Company]
            .isEmpty
        }
      )
    }
  )
}
