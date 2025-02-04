package com.rockthejvm.reviewboard.syntax

import zio.ZIO
import zio.test.{assertZIO, Assertion, TestResult}

extension [R, E, A](zio: ZIO[R, E, A]) {
  def assert(assertion: Assertion[A]): ZIO[R, E, TestResult] =
    assertZIO(zio)(assertion)

  // (=> A) => Boolean is a by-name parameter
  // by-name parameters are evaluated every time they are used
  def assert(predicate: (=> A) => Boolean): ZIO[R, E, TestResult] =
    assert(Assertion.assertion("predicate assertion")(predicate))
}
