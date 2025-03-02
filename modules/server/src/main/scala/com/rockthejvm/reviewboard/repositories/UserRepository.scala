package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.domain.data.User
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

trait UserRepository {
  def create(user: User): Task[User]
  def getById(id: Long): Task[Option[User]]
  def getByEmail(email: String): Task[Option[User]]
  def update(id: Long, ops: User => User): Task[User]
  def delete(id: Long): Task[Option[User]]
}

class UserRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepository {
  import quill.*

  inline given SchemaMeta[User] = schemaMeta[User]("users")
  inline given InsertMeta[User] = insertMeta[User](_.id)
  inline given UpdateMeta[User] = updateMeta[User](_.id)

  override def create(user: User): Task[User] =
    run {
      query[User]
        .insertValue(lift(user))
        .returning(user => user)
    }

  override def getById(id: Long): Task[Option[User]] =
    run {
      query[User]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByEmail(email: String): Task[Option[User]] =
    run {
      query[User]
        .filter(_.email == lift(email))
    }.map(_.headOption)

  override def update(id: Long, ops: User => User): Task[User] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Could not update, missing key $id"))
      updated <- run {
        query[User]
          .filter(_.id == lift(id))
          .updateValue(lift(ops(current)))
          .returning(user => user)
      }
    } yield updated

  override def delete(id: Long): Task[Option[User]] =
    for {
      user <- run {
        query[User]
          .filter(_.id == lift(id))
          .delete
          .returning(user => user)
      }
    } yield Some(user)
}

object UserRepositoryLive {
  val layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase.type]]
    } yield new UserRepositoryLive(quill)
  }
}
