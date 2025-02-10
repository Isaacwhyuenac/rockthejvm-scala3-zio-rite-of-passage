package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

case class RegisterUserAccount(
    email: String,
    password: String
) derives JsonCodec
