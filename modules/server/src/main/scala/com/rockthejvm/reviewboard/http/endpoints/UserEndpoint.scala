package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.UserToken
import com.rockthejvm.reviewboard.http.requests.{
  DeleteAccountRequest,
  LoginRequest,
  RegisterUserAccount,
  UpdatePasswordRequest
}
import com.rockthejvm.reviewboard.http.responses.UserResponse
import sttp.tapir.{path, stringToPath}
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.generic.auto.schemaForCaseClass

trait UserEndpoint extends BaseEndpoint {
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
  val updatePasswordEndpoint = baseEndpoint
    .tag(TAG)
    .name("update password")
    .description("Update the password of a user account")
    .in("users" / "password")
    .put
    .in(jsonBody[UpdatePasswordRequest])
    .out(jsonBody[UserResponse])

  val deleteEndpoint = baseEndpoint
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

}
