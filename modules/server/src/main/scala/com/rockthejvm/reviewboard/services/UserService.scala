package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserToken}
import com.rockthejvm.reviewboard.repositories.UserRepository
import com.rockthejvm.reviewboard.utils.Hasher
import zio.{Task, ZIO, ZLayer}

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def updatePassword(email: String, oldPassword: String, newPassword: String): Task[User]
  def deleteUser(email: String, password: String): Task[User]
  // JWT
  def generateToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (jwtService: JWTService, userRepo: UserRepository) extends UserService {
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

}

object UserServiceLive {
  val layer = ZLayer {
    for {
      jwtService <- ZIO.service[JWTService]
      repo       <- ZIO.service[UserRepository]
    } yield new UserServiceLive(jwtService, repo)
  }

}
