package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserToken}
import com.rockthejvm.reviewboard.repositories.{RecoveryTokensRepository, UserRepository}
import com.rockthejvm.reviewboard.utils.Hasher
import zio.{Task, ZIO, ZLayer}

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, password: String): Task[User]
  // JWT
  def generateToken(email: String, password: String): Task[Option[UserToken]]

  // password recovery flow
  def sendPasswordRecoveryToken(email: String): Task[Unit]
  def recoverPasswordFromToken(email: String, token: String, newPassword: String): Task[Boolean]
}

class UserServiceLive private (
    jwtService: JWTService,
    emailService: EmailService,
    userRepo: UserRepository,
    tokensRepo: RecoveryTokensRepository
) extends UserService {
  override def registerUser(email: String, password: String): Task[User] =
    for {
      user <- userRepo.create(User(0, email, Hasher.generateHash(password)))
    } yield user

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      existingUser <- userRepo.getByEmail(email)
      result <- existingUser match {
        case Some(user) =>
          ZIO.attempt(
            Hasher.validateHash(password, user.hashedPassword)
          )
        case None => ZIO.succeed(false)
      }
    } yield result

  override def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User] =
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"Cannot update password for user $email: inexistent"))
      verified <- ZIO.attempt(Hasher.validateHash(oldPassword, existingUser.hashedPassword))
      updatedUser <- userRepo
        .update(existingUser.id, _.copy(hashedPassword = Hasher.generateHash(newPassword)))
        .when(verified)
        .someOrFail(new RuntimeException(s"Cannot update password for user $email"))
    } yield updatedUser

  override def deleteUser(email: String, password: String): Task[User] =
    for {
      user <- userRepo.getByEmail(email).someOrFail(new RuntimeException(s"Cannot delete user $email: inexistent"))
      validPassword <- ZIO.attempt(Hasher.validateHash(password, user.hashedPassword))
      updatedUser <- userRepo
        .delete(user.id)
        .when(validPassword)
        .someOrFail(new RuntimeException(s"Cannot update password for user $email"))
    } yield updatedUser.get

  override def generateToken(email: String, password: String): Task[Option[UserToken]] =
    for {
      existingUser <- userRepo
        .getByEmail(email)
        .someOrFail(new RuntimeException(s"Cannot generate token for user $email: inexistent"))
      validPassword <- ZIO.attempt(Hasher.validateHash(password, existingUser.hashedPassword))
      maybeToken    <- jwtService.createToken(existingUser).when(validPassword)
    } yield maybeToken

  // Password recovery flow
  override def sendPasswordRecoveryToken(email: String): Task[Unit] =
    for {
      maybeToken <- tokensRepo.getToken(email)
      resp <- maybeToken match {
        case Some(token) => emailService.sendPasswordRecoveryEmail(email, token)
        case None        => ZIO.unit
      }
    } yield resp
    // get a token from the tokenRepo
    // email the token to the email

  override def recoverPasswordFromToken(email: String, token: String, newPassword: String): Task[Boolean] =
    for {
      existingUser <- userRepo.getByEmail(email).someOrFail(new RuntimeException("Non-existent user"))

      tokenIsValid <- tokensRepo.checkToken(email, token)
      result <- userRepo
        .update(existingUser.id, user => user.copy(hashedPassword = Hasher.generateHash(newPassword)))
        .when(tokenIsValid)
    } yield result.nonEmpty
}

object UserServiceLive {
  val layer = ZLayer {
    for {
      jwtService   <- ZIO.service[JWTService]
      emailService <- ZIO.service[EmailService]
      repo         <- ZIO.service[UserRepository]
      tokensRepo   <- ZIO.service[RecoveryTokensRepository]
    } yield new UserServiceLive(jwtService, emailService, repo, tokensRepo)
  }

}
