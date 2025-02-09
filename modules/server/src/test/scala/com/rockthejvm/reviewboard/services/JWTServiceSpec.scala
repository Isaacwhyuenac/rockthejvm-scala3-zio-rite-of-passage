package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.JWTConfig
import com.rockthejvm.reviewboard.domain.data.User
import com.rockthejvm.reviewboard.testdata.UserTestDataSpec
import zio.{Scope, ZIO, ZLayer}
import zio.test.{assertTrue, Spec, TestEnvironment, ZIOSpecDefault}

object JWTServiceSpec extends ZIOSpecDefault with UserTestDataSpec {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("JWTServiceSpec")(
      test("create and verify tokens") {
        for {
          service   <- ZIO.service[JWTService]
          userToken <- service.createToken(goodUser)
          userID    <- service.verifyToken(userToken.token)
        } yield assertTrue(
          userID.id == 1 && userID.email == "daniel@rockthejvm.com"
        )
      }
    )
      .provide(
        JWTServiceLive.layer,
        ZLayer.succeed(JWTConfig("secret", 3600, "rockthejvm.com"))
      )
}
