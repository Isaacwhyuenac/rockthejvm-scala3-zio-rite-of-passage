package com.rockthejvm.reviewboard.testdata

import com.rockthejvm.reviewboard.domain.data.User

trait UserTestDataSpec {

  val goodUser = User(
    id = 1L,
    email = "daniel@rockthejvm.com",
    hashedPassword = "1000:2cc8eaac7f5d06fae98110752a5ab17b0be5ba4d9a678d8e:be06c0241833f32601d3ff1fcc2be6969ef72f3bccade92e"
  )

  val badUser = User(
    id = 2L,
    email = "",
    hashedPassword = "1000:2cc8eaac7f5d06fae98110752a5ab17b0be5ba4d9a678d8e:be06c0241833f32601d3ff1fcc2be6969ef72f3bccade92e"
  )
}
