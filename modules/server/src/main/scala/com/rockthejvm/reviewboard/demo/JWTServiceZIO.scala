package com.rockthejvm.reviewboard.demo

import com.rockthejvm.reviewboard.config.{Configs, JWTConfig}
import com.rockthejvm.reviewboard.domain.data.User
import com.rockthejvm.reviewboard.services.{JWTService, JWTServiceLive}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object JWTServiceZIO extends ZIOAppDefault {
  val program = for {
    service   <- ZIO.service[JWTService]
    userToken <- service.createToken(User(1, "rockthejvm", "unimportant"))
    _         <- zio.Console.printLine(userToken)
    userId    <- service.verifyToken(userToken.token)
    _         <- zio.Console.printLine(s"User ID: $userId")
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    program
      .provide(
        JWTServiceLive.layer,
//        ZLayer.succeed(JWTConfig("secret", 30 * 24 * 60, "issuer"))
        ZLayer.fromZIO(Configs.makeConfig[JWTConfig]()("rockthejvm.jwt"))
      )
}
