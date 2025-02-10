package com.rockthejvm.reviewboard.http.controllers

import zio.Scope
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault}

object UserControllerSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("UserController")(
    )
}
