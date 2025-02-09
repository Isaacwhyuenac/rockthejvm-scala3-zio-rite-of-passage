package com.rockthejvm.reviewboard.config

case class JWTConfig(
    secret: String,
    ttl: Long,
    issuer: String
)
