package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.requests.{
  DeleteAccountRequest,
  ForgotPasswordRequest,
  LoginRequest,
  RecoverPasswordRequest,
  RegisterUserAccount,
  UpdatePasswordRequest
}
import com.rockthejvm.reviewboard.http.responses.UserResponse
import sttp.tapir.stringToPath
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass

trait UserEndpoints extends BaseEndpoint {
  private val TAG: String = "Users"

  val createUserEndpoint = baseEndpoint
    .tag(TAG)
    .name("register")
    .description("Register a user account with username and password")
    .in("users")
    .post
    .in(jsonBody[RegisterUserAccount])
    .out(jsonBody[UserResponse])

  // TODO: should be an authenticated endpoint (JWT)
  val updatePasswordEndpoint = secureBaseEndpoint
    .tag(TAG)
    .name("update password")
    .description("Update the password of a user account")
    .in("users" / "password")
    .put
    .in(jsonBody[UpdatePasswordRequest])
    .out(jsonBody[UserResponse])

  val deleteEndpoint = secureBaseEndpoint
    .tag(TAG)
    .name("delete")
    .description("Delete a user account")
    .in("users")
    .delete
    .in(jsonBody[DeleteAccountRequest])
    .out(jsonBody[UserResponse])

  val loginEndpoint = baseEndpoint
    .tag(TAG)
    .name("login")
    .description("Login with a user account")
    .in("users" / "login")
    .post
    .in(jsonBody[LoginRequest])
    .out(jsonBody[UserToken])

  // forget email flow
  val forgotPasswordEndpoint = baseEndpoint
    .tag(TAG)
    .name("forgot password")
    .description("Trigger email for password recovery")
    .in("users" / "forgot")
    .post
    .in(jsonBody[ForgotPasswordRequest])

  // recover password flow
  // /user/recover { email, token, newPassword }
  val recoverPasswordEndpoint = baseEndpoint
    .tag(TAG)
    .name("recover password")
    .description("Set new password based on OTP")
    .in("users" / "recover")
    .post
    .in(jsonBody[RecoverPasswordRequest])

}
