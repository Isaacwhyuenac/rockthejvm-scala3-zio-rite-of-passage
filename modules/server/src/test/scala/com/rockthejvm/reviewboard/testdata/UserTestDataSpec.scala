package com.rockthejvm.reviewboard.testdata

import com.rockthejvm.reviewboard.domain.data.User

trait UserTestDataSpec {

  val goodUser = User(
    id = 1L,
    email = "daniel@rockthejvm.com",
    hashedPassword = "rockthejvm"
  )

  val badUser = User(
    id = 2L,
    email = "",
    hashedPassword = "rockthejvm"
  )
}
