package com.rockthejvm.reviewboard.repositories

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

object Repository {
  val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)

  val dataSourceLayer = Quill.DataSource.fromPrefix("rockthejvm.db")

  val dataLayer = dataSourceLayer >>> quillLayer
}
