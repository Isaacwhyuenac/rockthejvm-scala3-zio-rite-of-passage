package com.rockthejvm.reviewboard

import com.rockthejvm.reviewboard.http.HttpApi
import com.rockthejvm.reviewboard.repositories.{CompanyRepositoryLive, Repository}
import com.rockthejvm.reviewboard.services.{CompanyService, CompanyServiceLive}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.http.Server
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Application extends ZIOAppDefault {
  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(endpoints)
    )
    _ <- zio.Console.printLine("Rock the JVM!")
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    serverProgram.provide(
      Server.default,
 
      Repository.dataLayer,
      // services
      CompanyServiceLive.layer,
      
      // repositories
      CompanyRepositoryLive.layer,
    )

}
