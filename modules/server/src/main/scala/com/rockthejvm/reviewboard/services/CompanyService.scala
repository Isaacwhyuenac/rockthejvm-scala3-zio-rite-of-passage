package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.repositories.CompanyRepository
import zio.{Task, ZIO, ZLayer}

import scala.collection.mutable

trait CompanyService {
  def create(req: CreateCompanyRequest): Task[Company]
  def getAll(): Task[List[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
}

object CompanyService {
  val dummyLive = ZLayer.succeed(new CompanyServiceDummy)
}

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService {

  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll(): Task[List[Company]] =
    repo.getAll

  override def getById(id: Long): Task[Option[Company]] =
    repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] =
    repo.getBySlug(slug)
}

object CompanyServiceLive {
  def layer =
    ZLayer {
      for {
        repo <- ZIO.service[CompanyRepository]
      } yield CompanyServiceLive(repo)
    }
}

class CompanyServiceDummy extends CompanyService {
  val db = mutable.Map[Long, Company]()

  override def create(req: CreateCompanyRequest): Task[Company] =
    ZIO.succeed {
      val newId: Long = db.keys.maxOption.getOrElse(0L) + 1
      val newCompany  = req.toCompany(newId)
      db += (newId -> newCompany)
      newCompany
    }

  override def getAll(): Task[List[Company]] =
    ZIO.succeed {
      db.values.toList
    }

  override def getById(id: Long): Task[Option[Company]] =
    ZIO.succeed {
      db.get(id)
    }

  override def getBySlug(slug: String): Task[Option[Company]] =
    ZIO.succeed(
      db.values.find(_.slug == slug)
    )
}
