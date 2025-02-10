package com.rockthejvm.reviewboard.integration

import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.controllers.UserController
import com.rockthejvm.reviewboard.http.requests.{
  DeleteAccountRequest,
  LoginRequest,
  RegisterUserAccount,
  UpdatePasswordRequest
}
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.repositories.{Repository, RepositorySpec, UserRepository, UserRepositoryLive}
import com.rockthejvm.reviewboard.services.{JWTService, JWTServiceLive, UserService, UserServiceLive}
import sttp.client3.{basicRequest, SttpBackend, UriContext}
import sttp.client3.testing.SttpBackendStub
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.{Scope, Task, ZIO, ZLayer}
import zio.test.{assertTrue, Spec, TestEnvironment, ZIOSpecDefault}
import zio.json.{DecoderOps, EncoderOps, JsonCodec}

object UserFlowSpec extends ZIOSpecDefault with RepositorySpec {
  private val EMAIL    = "daniel@rockthejvm.com"
  private val PASSWORD = "rockthejvm"
  // http controller
  // service
  // repository
  // test containers
  override val sqlScript: String = "sql/users.sql"

  // requirement for `backendStubZIO` to work
  private given zioMonad: sttp.monad.MonadError[zio.Task] = RIOMonadError[Any]

  private val backendStubZIO: ZIO[UserService & JWTService, Nothing, SttpBackend[Task, Any]] =
    for {
      /// create the controller
      companyController <- UserController.makeZIO
      /// build tapir backend
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointsRunLogic(companyController.routes)
          .backend()
      )
    } yield backendStub

  extension [A: JsonCodec](backend: SttpBackend[Task, Any])
    def sendRequest[B: JsonCodec](
        method: Method,
        path: String,
        payload: A,
        maybeToken: Option[String] = None
    ): Task[Option[B]] =
      basicRequest
        .method(method, uri"$path")
        .body(payload.toJson)
        .auth
        .bearer(maybeToken.getOrElse(""))
        .send(backend)
        .map(_.body)
        .map(_.toOption.flatMap(payload => payload.fromJson[B].toOption))

    def post[B: JsonCodec](path: String, payload: A, maybeToken: Option[String] = None): Task[Option[B]] =
      sendRequest(Method.POST, path, payload, maybeToken)

    def put[B: JsonCodec](path: String, payload: A, maybeToken: Option[String] = None): Task[Option[B]] =
      sendRequest(Method.PUT, path, payload, maybeToken)

    def delete[B: JsonCodec](path: String, payload: A, maybeToken: Option[String] = None): Task[Option[B]] =
      sendRequest(Method.DELETE, path, payload, maybeToken)

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserFlowSpec")(
      test("basic test") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub
            .post[UserResponse]("/users", RegisterUserAccount(EMAIL, PASSWORD))
        } yield assertTrue(maybeResponse.contains(UserResponse(EMAIL)))
      },
      test("create and log in") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub
            .post[UserResponse]("/users", RegisterUserAccount(EMAIL, PASSWORD))
          maybeToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest(EMAIL, PASSWORD))
        } yield assertTrue(
          maybeToken.filter(_.email == EMAIL).nonEmpty
        )
      },
      test("update password") {
        for {
          backendStub <- backendStubZIO
          maybeResponse <- backendStub
            .post[UserResponse]("/users", RegisterUserAccount(EMAIL, PASSWORD))
          userToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest(EMAIL, PASSWORD))
            .someOrFail(new RuntimeException("Authentication failed"))
          response <- backendStub
            .put[UserResponse](
              "/users/password",
              UpdatePasswordRequest(EMAIL, PASSWORD, "newpassword"),
              Some(userToken.token)
            )
          maybeOldToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest(EMAIL, PASSWORD))
          maybeNewToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest(EMAIL, "newpassword"))
        } yield assertTrue(
          maybeOldToken.isEmpty && maybeNewToken.nonEmpty
        )
      },
      test("delete password") {
        for {
          backendStub    <- backendStubZIO
          userRepository <- ZIO.service[UserRepository]
          maybeResponse <- backendStub
            .post[UserResponse]("/users", RegisterUserAccount(EMAIL, PASSWORD))
          maybeOldUser <- userRepository.getByEmail(EMAIL)
          userToken <- backendStub
            .post[UserToken]("/users/login", LoginRequest(EMAIL, PASSWORD))
            .someOrFail(new RuntimeException("Authentication failed"))
          _ <- backendStub
            .delete[UserResponse](
              "/users",
              DeleteAccountRequest(EMAIL, PASSWORD),
              Some(userToken.token)
            )
          maybeUser <- userRepository.getByEmail(EMAIL)
        } yield assertTrue(
          maybeOldUser.filter(_.email == EMAIL).nonEmpty && maybeUser.isEmpty
        )
      }
    )
      .provide(
        UserServiceLive.layer,
        JWTServiceLive.layer,
        UserRepositoryLive.layer,
        Repository.quillLayer,
        dataSourceLayer,
        ZLayer.succeed(JWTConfig("secret", 3600, "rockthejvm.com")),
        Scope.default
      )

}
