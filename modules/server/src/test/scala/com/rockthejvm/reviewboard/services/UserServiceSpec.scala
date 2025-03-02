package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.{User, UserID, UserToken}
import com.rockthejvm.reviewboard.repositories.{RecoveryTokensRepository, UserRepository}
import com.rockthejvm.reviewboard.services.UserServiceSpec.test
import com.rockthejvm.reviewboard.testdata.UserTestDataSpec
import zio.{Scope, Task, ZIO, ZLayer}
import zio.test.{assertTrue, Spec, TestEnvironment, ZIOSpecDefault}

import scala.collection.mutable

object UserServiceSpec extends ZIOSpecDefault with UserTestDataSpec {

  val stubRepoLayer = ZLayer.succeed {
    new UserRepository {

      val db = mutable.Map[Long, User](1L -> goodUser)
      override def create(user: User): Task[User] =
        ZIO.succeed {
          db += (user.id -> user)
          user
        }

      override def getById(id: Long): Task[Option[User]] =
        ZIO.succeed(db.get(id))

      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(db.values.find(_.email == email))

      override def update(id: Long, ops: User => User): Task[User] =
        ZIO.attempt {
          val updatedUser = ops(db(id))
          db += (id -> updatedUser)
          updatedUser
        }

      override def delete(id: Long): Task[Option[User]] =
        ZIO.attempt {
          val deletedUser = db.remove(id)
          deletedUser
        }
    }
  }

  val stubJwtLayer = ZLayer.succeed {
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user.email, "ALL_IS_GOOD", Long.MaxValue))

      override def verifyToken(token: String): Task[UserID] =
        ZIO.succeed(UserID(goodUser.id, goodUser.email))
    }
  }

  val stubEmailServiceLayer = ZLayer.succeed {
    new EmailService {
      override def sendEmail(email: String, subject: String, content: String): Task[Unit] =
        ZIO.unit

      override def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit] = ZIO.unit
    }
  }

  val stubRecoveryTokensRepoLayer = ZLayer.succeed {
    new RecoveryTokensRepository {
      val db = mutable.Map[String, String]()

      override def getToken(email: String): Task[Option[String]] =
        for {
          _     <- zio.test.TestRandom.feedStrings("test-token")
          token <- zio.Random.nextString(8)
          _ <- ZIO.attempt {
            db += (email -> token)
          }
        } yield Some(token)

      override def checkToken(email: String, token: String): Task[Boolean] =
        ZIO.succeed {
          db.get(email).contains(token)
        }
    }
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserServiceSpec")(
      test("create and validate a user") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.registerUser(goodUser.email, "rockthejvm")
          valid   <- service.verifyPassword(goodUser.email, "rockthejvm")
        } yield assertTrue(valid)
      },
      test("validate correct credential") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword(goodUser.email, "rockthejvm")
        } yield assertTrue(valid)
      },
      test("validate incorrect credential") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword(goodUser.email, "somethingelse")
        } yield assertTrue(!valid)
      },
      test("validate non-existent credential") {
        for {
          service <- ZIO.service[UserService]
          valid   <- service.verifyPassword("someoneelse@gmail.com", "somethingelse")
        } yield assertTrue(!valid)
      },
      test("update password") {
        for {
          service  <- ZIO.service[UserService]
          _        <- service.updatePassword(goodUser.email, "rockthejvm", "newpassword")
          oldValid <- service.verifyPassword(goodUser.email, "rockthejvm")
          newValid <- service.verifyPassword(goodUser.email, "newpassword")
        } yield assertTrue(newValid && !oldValid)
      },
      test("delete non-existent user should fail") {
        for {
          service <- ZIO.service[UserService]
          err     <- service.deleteUser("someone@gmail.com", "rockthejvm").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete with wrong password should fail") {
        for {
          service <- ZIO.service[UserService]
          err     <- service.deleteUser(goodUser.email, "wrongpassword").flip
        } yield assertTrue(err.isInstanceOf[RuntimeException])
      },
      test("delete user") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.deleteUser(goodUser.email, "rockthejvm")
        } yield assertTrue(user.email == goodUser.email)
      }
    )
      .provide(
        UserServiceLive.layer,
        stubRepoLayer,
        stubJwtLayer,
        stubEmailServiceLayer,
        stubRecoveryTokensRepoLayer
      )
}
