package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.UserID
import com.rockthejvm.reviewboard.domain.errors.Unauthorized
import com.rockthejvm.reviewboard.http.endpoints.UserEndpoints
import com.rockthejvm.reviewboard.http.requests.RegisterUserAccount
import com.rockthejvm.reviewboard.http.responses.UserResponse
import com.rockthejvm.reviewboard.services.{JWTService, UserService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

class UserController private (jwtService: JWTService, userService: UserService)
    extends BaseController
    with UserEndpoints {

  val create: ServerEndpoint[Any, Task] = createUserEndpoint.serverLogic { (req: RegisterUserAccount) =>
    userService
      .registerUser(req.email, req.password)
      .map(user => UserResponse(user.email))
      .either
  }

  val login: ServerEndpoint[Any, Task] = loginEndpoint
    .serverLogic { req =>
      userService
        .generateToken(req.email, req.password)
        .someOrFail(Unauthorized)
        .either
    }

  // change password
  val updatePassword: ServerEndpoint[Any, Task] = updatePasswordEndpoint
//    .securityIn(auth.bearer[String]()) // header: "Authorization: Bearer <token>"
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { userID => req =>
      userService
        .updatePassword(req.email, req.oldPassword, req.newPassword)
        .map(user => UserResponse(user.email))
        .either
    }

  // delete account
  val deleteAccount: ServerEndpoint[Any, Task] = deleteEndpoint
//    .securityIn(auth.bearer[String]()) // header: "Authorization: Bearer <token>"
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { userID => req =>
      userService
        .deleteUser(req.email, req.password)
        .map(user => UserResponse(user.email))
        .either
    }

  val forgotPassword: ServerEndpoint[Any, Task] = forgotPasswordEndpoint.serverLogic { req =>
    userService.sendPasswordRecoveryToken(req.email).either
  }

  val recoverPassword: ServerEndpoint[Any, Task] = recoverPasswordEndpoint
    .serverLogic { req =>
      userService
        .recoverPasswordFromToken(req.email, req.token, req.newPassword)
        .filterOrFail(identity(_))(Unauthorized)
        .unit
        .either
    }

  override val routes: List[ServerEndpoint[Any, Task]] =
    List(create, login, updatePassword, deleteAccount, forgotPassword, recoverPassword)

}

object UserController {

  val makeZIO = for {
    jwtService  <- ZIO.service[JWTService]
    userService <- ZIO.service[UserService]
  } yield new UserController(jwtService, userService)

}
