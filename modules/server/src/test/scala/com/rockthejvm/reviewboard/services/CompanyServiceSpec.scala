package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import zio.{Scope, Task, ZIO, ZLayer}
import zio.test.{assertZIO, Assertion, Spec, TestEnvironment, ZIOSpecDefault}

import scala.collection.mutable

object CompanyServiceSpec extends ZIOSpecDefault {

  val service = ZIO.serviceWithZIO[CompanyService]

  val stubRepository = ZLayer.succeed(new CompanyRepository {

    val db: mutable.Map[Long, Company] = mutable.Map[Long, Company]()
    override def create(company: Company): Task[Company] =
      ZIO.succeed {
        val newId: Long = db.keys.maxOption.getOrElse(0L) + 1
        val newCompany  = company.copy(id = newId, slug = Company.makeSlug(company.name))
        db += (newId -> newCompany)
        newCompany
      }

    override def update(id: Long, ops: Company => Company): Task[Company] =
      ZIO.attempt {
        val current = db(id)
        val updated = ops(current)
        db += (id -> updated)
        updated
      }

    override def delete(id: Long): Task[Option[Company]] =
      ZIO.attempt {
        val company = db(id)
        db -= id
        Some(company)
      }

    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed(db.get(id))

    override def getBySlug(slug: String): Task[Option[Company]] =
      ZIO.succeed(db.values.find(_.slug == slug))

    override def getAll: Task[List[Company]] =
      ZIO.succeed(db.values.toList)
  })

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("CompanyServiceSpec")(
      test("create") {
        val companyZIO: ZIO[CompanyService, Throwable, Company] =
          service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))

        assertZIO(companyZIO)(
          Assertion.assertion("company") { company =>
            company.name == "Rock the JVM"
            && company.url == "rockthejvm.com"
            && company.slug == "rock-the-jvm"
          }
        )
      },
      test("get by id") {
        val companyZIO =
          for {
            created   <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            retrieved <- service(_.getById(created.id))
          } yield (created, retrieved)

        assertZIO(companyZIO)(
          Assertion.assertion("company") {
            //
            case (createdCompany, Some(retrievedCompany)) =>
              createdCompany.name == "Rock the JVM"
              && createdCompany.url == "rockthejvm.com"
              && createdCompany.slug == "rock-the-jvm"
              && createdCompany == retrievedCompany
            case _ => false
          }
        )
      },
      test("get by slug") {
        val companyZIO =
          for {
            created   <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            retrieved <- service(_.getBySlug(created.slug))
          } yield (created, retrieved)

        assertZIO(companyZIO)(
          Assertion.assertion("company") {
            //
            case (createdCompany, Some(retrievedCompany)) =>
              createdCompany.name == "Rock the JVM"
              && createdCompany.url == "rockthejvm.com"
              && createdCompany.slug == "rock-the-jvm"
              && createdCompany == retrievedCompany
            case _ => false
          }
        )
      },
      test("get all") {
        val companiesZIO =
          for {
            company   <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
            company2  <- service(_.create(CreateCompanyRequest("Google", "google.com")))
            companies <- service(_.getAll())
          } yield (company, company2, companies)

        assertZIO(companiesZIO)(
          Assertion.assertion("companies") {
            //
            case (company, company2, retrievedCompany :: retrievedCompany2 :: Nil) =>
              company.name == "Rock the JVM"
              && company.url == "rockthejvm.com"
              && company.slug == "rock-the-jvm"
              && company == retrievedCompany
              && company2.name == "Google"
              && company2.url == "google.com"
              && company2.slug == "google"
              && company2 == retrievedCompany2
          }
        )
      }
    )
      .provide(CompanyServiceLive.layer, stubRepository)

}
