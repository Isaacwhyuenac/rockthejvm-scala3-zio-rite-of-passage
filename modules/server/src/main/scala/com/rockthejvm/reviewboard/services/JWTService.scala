package com.rockthejvm.reviewboard.services

import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.rockthejvm.reviewboard.domain.data.{User, UserID, UserToken}
import zio.{Clock, Task, ZIO, ZLayer}

trait JWTService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserID]
}

class JWTServiceLive(clock: java.time.Clock) extends JWTService {
  private val secret         = "secret" // TODO: move to config
  private val TTL            = 30 * 24 * 3600
  private val ISSUER         = "rockthejvm.com"
  private val CLAIM_USERNAME = "username"
  private val algorithm      = Algorithm.HMAC512(secret)
  private val verifier = com.auth0.jwt.JWT
    .require(algorithm)
    .withIssuer(ISSUER)
    .asInstanceOf[BaseVerification]
    .build(clock)

  override def createToken(user: User): Task[UserToken] =
    for {
      now <- ZIO.attempt(clock.instant())
      expiration = now.plusSeconds(TTL)
      jwt <- ZIO.attempt(
        com.auth0.jwt.JWT
          .create()
          .withIssuer(ISSUER)
          .withIssuedAt(now)
          .withExpiresAt(expiration)
          .withSubject(user.id.toString)
          .withClaim(CLAIM_USERNAME, user.email)
          .sign(algorithm)
      )
    } yield UserToken(user.email, jwt, expiration.getEpochSecond)

  override def verifyToken(token: String): Task[UserID] =
    for {
      decoded <- ZIO.attempt(verifier.verify(token))
      userID <- ZIO.attempt(
        UserID(
          decoded.getSubject.toLong,
          decoded.getClaim(CLAIM_USERNAME).asString()
        )
      )
    } yield userID
}

object JWTServiceLive {
  val layer = ZLayer {
    for {
      javaClock <- zio.Clock.javaClock
    } yield new JWTServiceLive(javaClock)
  }
}
