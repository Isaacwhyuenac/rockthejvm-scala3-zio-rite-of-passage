package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.testdata.CompanyTestDataSpec
import zio.{Scope, ZIO, ZLayer}
import zio.test.{Assertion, Spec, TestEnvironment, ZIOSpecDefault, assertZIO}

import java.sql.SQLException
import javax.sql.DataSource

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec with CompanyTestDataSpec {

  private def genString = scala.util.Random.alphanumeric.take(10).mkString
  private def genCompany(): Company =
    Company(
      id = -1L,
      slug = genString,
      name = genString,
      url = genString
    )

  override val sqlScript: String = "sql/companies.sql"

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyRepositorySpec")(
      test("create a company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rockthejvm)
        } yield company

        assertZIO(program)(
          Assertion.assertion("company") {
            case Company(_, "rock-the-jvm", "Rock the JVM", "rockthejvm.com", _, _, _, _, _) => true
            case _                                                                           => false
          }
        )
      },
      test("create a duplicate should error") {
        val program = for {
          repo  <- ZIO.service[CompanyRepository]
          _     <- repo.create(rockthejvm)
          error <- repo.create(rockthejvm).flip
        } yield error

        assertZIO(program)(
          Assertion.assertion("company") {
            _.isInstanceOf[SQLException]
          }
        )
      },

      test("get by id and slug") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(rockthejvm)
          byId    <- repo.getById(1)
          bySlug  <- repo.getBySlug("rock-the-jvm")
        } yield (company, byId, bySlug)

        assertZIO(program)(
          Assertion.assertion("company") { case (company, fetchedById, fetchedBySlug) =>
            fetchedById.contains(company) && fetchedBySlug.contains(company)
          }
        )
      },
      test("update a company") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(rockthejvm)
          updated     <- repo.update(company.id, _.copy(name = "Rock the JVM 2"))
          fetchedById <- repo.getById(company.id)
        } yield (updated, fetchedById)

        assertZIO(program)(
          Assertion.assertion("company") { case (updated, Some(fetchedById)) =>
            updated.name == "Rock the JVM 2" && fetchedById.name == "Rock the JVM 2"
          }
        )
      },
      test("delete a company") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(rockthejvm)
          _           <- repo.delete(company.id)
          fetchedById <- repo.getById(company.id)
        } yield fetchedById

        assertZIO(program)(
          Assertion.assertion("company") {
            _.isEmpty
          }
        )
      },
      test("get all records") {
        val program = for {
          repo             <- ZIO.service[CompanyRepository]
          company          <- ZIO.collectAll((1 to 10).map(i => repo.create(genCompany())))
          companiesFetched <- repo.getAll
        } yield (company, companiesFetched)

        assertZIO(program)(
          Assertion.assertion("companies") { case (created, fetched) =>
            created.toSet == fetched.toSet
          }
        )
      }
    ).provide(
      CompanyRepositoryLive.layer,
      Repository.quillLayer,
      dataSourceLayer,
      Scope.default
    )

}
