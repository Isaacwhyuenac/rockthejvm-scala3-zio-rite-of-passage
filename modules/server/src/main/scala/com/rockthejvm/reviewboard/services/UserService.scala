package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserToken}
import com.rockthejvm.reviewboard.repositories.UserRepository
import com.rockthejvm.reviewboard.utils.Hasher
import zio.{Task, ZIO, ZLayer}

trait UserService {
  def registerUser(email: String, password: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]

  // JWT
  def generateToken(email: String, password: String): Task[Option[UserToken]]
}

class UserServiceLive private (jwtService: JWTService, userRepo: UserRepository) extends UserService {
  override def registerUser(email: String, password: String): Task[User] =
    for {
      user <- userRepo.create(User(0, email, Hasher.hashPassword(password)))
    } yield user

  override def verifyPassword(email: String, password: String): Task[Boolean] =
    for {
      user   <- userRepo.getByEmail(email).someOrFail(new RuntimeException(s"Cannot verify user $email: inexistent"))
      result <- ZIO.attempt(Hasher.validateHash(password, user.hashedPassword))
    } yield result

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
