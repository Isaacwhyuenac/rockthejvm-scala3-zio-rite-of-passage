package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Company
import io.getquill.{
  autoQuote,
  defaultParser,
  insertMeta,
  query,
  schemaMeta,
  updateMeta,
  InsertMeta,
  SchemaMeta,
  SnakeCase,
  UpdateMeta
}
import io.getquill.jdbczio.Quill
import zio.{Task, ZIO, ZLayer}

trait CompanyRepository {
  def create(company: Company): Task[Company]
  def update(id: Long, ops: Company => Company): Task[Company]
  def delete(id: Long): Task[Option[Company]]
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]
}

class CompanyRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {

  import quill.*

  inline given schema: SchemaMeta[Company]  = schemaMeta[Company]("companies")
  inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)
  inline given upsMeta: UpdateMeta[Company] = updateMeta[Company](_.id)

  override def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company))
        .returning(company => company)
    }

  override def update(id: Long, ops: Company => Company): Task[Company] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Could not update, missing key $id"))
      updated <- run {
        query[Company]
          .filter(_.id == lift(id))
          .updateValue(lift(ops(current)))
          .returning(company => company)
      }
    } yield updated

  override def delete(id: Long): Task[Option[Company]] = 
    for {
        company <- run { query[Company]
            .filter(_.id == lift(id))
            .delete
            .returning(company => company)}
        } yield Some(company)
    
  

  override def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.slug == lift(slug))
    }.map(_.headOption)

  override def getAll: Task[List[Company]] =
    run {
      query[Company]
    }
}

object CompanyRepositoryLive {
  def layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
    } yield CompanyRepositoryLive(quill)
  }
}
