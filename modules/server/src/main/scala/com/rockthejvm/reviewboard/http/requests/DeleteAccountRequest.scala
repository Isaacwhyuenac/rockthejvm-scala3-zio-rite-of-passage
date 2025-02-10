package com.rockthejvm.reviewboard.http.requests

import zio.json.JsonCodec

case class DeleteAccountRequest(
    email: String,
    password: String
) derives JsonCodec
