package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.Review
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

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def update(id: Long, ops: Review => Review): Task[Review]
  def delete(id: Long): Task[Option[Review]]
}

class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {

  import quill.*

  inline given reviewSchema: SchemaMeta[Review]     = schemaMeta[Review]("reviews")
  inline given reviewInsertMeta: InsertMeta[Review] = insertMeta[Review](_.id, _.created, _.updated)
  inline given reviewUpdateMeta: UpdateMeta[Review] =
    updateMeta[Review](_.id, _.companyId, _.userId, _.created, _.updated)

  override def create(review: Review): Task[Review] =
    run {
      query[Review]
        .insertValue(lift(review))
        .returning(review => review)
    }

  override def getById(id: Long): Task[Option[Review]] =
    run {
      query[Review]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByCompanyId(companyId: Long): Task[List[Review]] =
    run {
      query[Review]
        .filter(_.companyId == lift(companyId))
    }

  override def getByUserId(userId: Long): Task[List[Review]] =
    run {
      query[Review]
        .filter(_.userId == lift(userId))
    }

  override def update(id: Long, ops: Review => Review): Task[Review] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Could not update, missing key $id"))
      updated <- run {
        query[Review]
          .filter(_.id == lift(id))
          .updateValue(lift(ops(current)))
          .returning(review => review)
      }
    } yield updated

  override def delete(id: Long): Task[Option[Review]] =
    for {
      review <- run {
        query[Review]
          .filter(_.id == lift(id))
          .delete
          .returning(review => review)
      }
    } yield Some(review)
}

object ReviewRepositoryLive {
  def layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
    } yield ReviewRepositoryLive(quill)
  }
}
