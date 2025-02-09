package com.rockthejvm.reviewboard.demo

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

import java.time.Instant

object JWTServiceDemo {
  def main(args: Array[String]): Unit = {
    val algorithm = Algorithm.HMAC512("secret")
    val jwt = JWT
      .create()
      .withIssuer("rockthejvm.com")
      .withIssuedAt(Instant.now())
      .withExpiresAt(Instant.now().plusSeconds(30 * 24 * 3600))
      .withSubject("1") // user identifier
      .withClaim("username", "daniel@rockthejvm.com")
      .sign(algorithm)

    println(jwt)

    // verification
    val verifier = JWT
      .require(algorithm)
      .withIssuer("rockthejvm.com")
      .asInstanceOf[com.auth0.jwt.JWTVerifier.BaseVerification]
      .build(java.time.Clock.systemDefaultZone())

    val decoded  = verifier.verify(jwt)
    val userId   = decoded.getSubject
    val username = decoded.getClaim("username").asString()

    println("userId = " + userId)
    println("username = " + username)
  }
}
