package com.rockthejvm.reviewboard.config

case class EmailServiceConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    sender: String
)
