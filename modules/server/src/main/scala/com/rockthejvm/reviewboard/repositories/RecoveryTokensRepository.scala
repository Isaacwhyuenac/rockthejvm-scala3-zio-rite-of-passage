package com.rockthejvm.reviewboard.repositories

import com.rockthejvm.reviewboard.config.{Configs, RecoveryTokensConfig}
import com.rockthejvm.reviewboard.domain.data.PasswordRecoveryToken
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

import java.time.temporal.ChronoUnit

trait RecoveryTokensRepository {
  def getToken(email: String): Task[Option[String]]

  def checkToken(email: String, token: String): Task[Boolean]
}

class RecoveryTokensRepositoryLive private (
    tokenConfig: RecoveryTokensConfig,
    quill: Quill.Postgres[SnakeCase],
    userRepository: UserRepository
) extends RecoveryTokensRepository {

  import quill._

  inline given SchemaMeta[PasswordRecoveryToken] = schemaMeta[PasswordRecoveryToken]("recovery_tokens")
  inline given InsertMeta[PasswordRecoveryToken] = insertMeta[PasswordRecoveryToken]() // no field needs to be omitted
  inline given UpdateMeta[PasswordRecoveryToken] = updateMeta[PasswordRecoveryToken](_.email)

  private def randomUppercaseString(length: Int): Task[String] = zio.Random.nextString(length).map(_.toUpperCase)

  private def findToken(email: String): Task[Option[String]] =
    run {
      query[PasswordRecoveryToken]
        .filter(_.email == lift(email))
    }
      .map(_.headOption.map(_.token))

  private def replaceToken(email: String): Task[String] =
    for {
      token <- randomUppercaseString(8)
      time  <- zio.Clock.currentTime(ChronoUnit.MILLIS)
      _ <- run {
        query[PasswordRecoveryToken]
          .updateValue(
            lift(
              PasswordRecoveryToken(email, token, time + tokenConfig.duration)
            )
          )
          .returning(r => r)
      }
    } yield token

  private def generateToken(email: String): Task[String] =
    for {
      token <- randomUppercaseString(8)
      _ <- run {
        query[PasswordRecoveryToken]
          .insertValue(
            lift(
              PasswordRecoveryToken(email, token, System.currentTimeMillis() + tokenConfig.duration)
            )
          )
          .returning(r => r)
      }
    } yield token

  private def makeFreshToken(email: String): Task[String] =
    // find token in the table
    // if so, replace
    // if not, insert
    findToken(email).flatMap {
      case Some(_) => replaceToken(email)
      case None    => generateToken(email)
    }

  override def getToken(email: String): Task[Option[String]] =
    // check the user in the database
    // if the user exists, generate a token, store it in the database, and return it
    userRepository
      .getByEmail(email)
      .flatMap {
        case None    => ZIO.none
        case Some(_) => makeFreshToken(email).map(Some(_))
      }

  override def checkToken(email: String, token: String): Task[Boolean] =
    run {
      query[PasswordRecoveryToken]
        .filter(recoverytoken => recoverytoken.email == lift(email) && recoverytoken.token == lift(token))
    }.map(_.nonEmpty)
}

object RecoveryTokensRepositoryLive {

  val layer = ZLayer {
    for {
      config   <- ZIO.service[RecoveryTokensConfig]
      quill    <- ZIO.service[Quill.Postgres[SnakeCase.type]]
      userRepo <- ZIO.service[UserRepository]
    } yield new RecoveryTokensRepositoryLive(config, quill, userRepo)
  }

  val configuredLayer = Configs.makeConfigLayer[RecoveryTokensConfig]()("rockthejvm.recoverytokens") >>> layer

}
