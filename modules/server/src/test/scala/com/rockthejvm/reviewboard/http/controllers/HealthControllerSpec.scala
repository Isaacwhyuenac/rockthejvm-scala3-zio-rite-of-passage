package com.rockthejvm.reviewboard.http.controllers

import sttp.client3.{basicRequest, UriContext}
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.{Scope, Task, ZIO}
import zio.test.{assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}

object HealthControllerSpec extends ZIOSpecDefault {

  // requirement for `backendStubZIO` to work
  private given zioMonad: sttp.monad.MonadError[zio.Task] = RIOMonadError[Any]

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("HealthControllerSpec")(
      test("health check") {
        // test logic
        val program = for {
          controller <- HealthController.makeZIO
          endpoint   <- ZIO.succeed(controller.health)
          backendStub <- ZIO.succeed(
            TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
              .whenServerEndpointRunLogic(endpoint)
              .backend()
          )
          response <- basicRequest.get(uri"/health").send(backendStub)
        } yield response.body

        assertZIO(program) {
          Assertion.assertion("works") { respBody =>
            respBody.toOption.contains("All good")
          }
        }
      }
    )

}
