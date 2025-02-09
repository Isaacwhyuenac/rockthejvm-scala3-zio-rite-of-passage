package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.repositories.ReviewRepositorySpec.dataSourceLayer
import com.rockthejvm.reviewboard.testdata.UserTestDataSpec
import zio.{Scope, ZIO}
import zio.test.{assertTrue, assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}

object UserRepositorySpec extends ZIOSpecDefault with RepositorySpec with UserTestDataSpec {

  override val sqlScript: String = "sql/users.sql"

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserRepositorySpec")(
      test("create user") {
        val program = for {
          repo <- ZIO.service[UserRepository]
          user <- repo.create(goodUser)
        } yield user

        assertZIO(program)(
          Assertion.assertion("users are equal") { case user =>
            user.email == goodUser.email &&
            user.hashedPassword == goodUser.hashedPassword
          }
        )
      },
      test("get user by id") {
        for {
          repo        <- ZIO.service[UserRepository]
          user        <- repo.create(goodUser)
          fetchedUser <- repo.getById(user.id)
        } yield assertTrue(fetchedUser.contains(user))
      },
      test("edit user") {
        for {
          repo        <- ZIO.service[UserRepository]
          user        <- repo.create(goodUser)
          updatedUser <- repo.update(user.id, _.copy(email = "new user"))
          fetchedUser <- repo.getById(user.id)
        } yield assertTrue(fetchedUser.contains(updatedUser))
      },
      test("delete user") {
        for {
          repo        <- ZIO.service[UserRepository]
          user        <- repo.create(goodUser)
          _           <- repo.delete(user.id)
          fetchedUser <- repo.getById(user.id)
        } yield assertTrue(fetchedUser.isEmpty)
      }
    )
      .provide(
        UserRepositoryLive.layer,
        Repository.quillLayer,
        dataSourceLayer,
        Scope.default
      )

}
