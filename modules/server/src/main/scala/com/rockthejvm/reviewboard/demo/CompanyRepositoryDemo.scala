package com.rockthejvm.reviewboard.demo

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.repositories.{CompanyRepository, CompanyRepositoryLive}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object CompanyRepositoryDemo extends ZIOAppDefault {
  val program = for {
    repo <- ZIO.service[CompanyRepository]
    _ <- repo.create(
      Company(
        -1,
        "rock-the-jvm",
        "Rock the JVM",
        "rockthejvm.com"
      )
    )
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program
      .provide(
        Quill.Postgres.fromNamingStrategy(SnakeCase),
        Quill.DataSource.fromPrefix("rockthejvm.db"),
        CompanyRepositoryLive.layer,
      )
}
