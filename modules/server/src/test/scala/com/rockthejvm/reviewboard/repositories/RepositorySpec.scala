package com.rockthejvm.reviewboard.repositories

import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import zio.{Scope, ZIO, ZLayer}

import javax.sql.DataSource

trait RepositorySpec {
  val sqlScript: String

  // test containers
  // spawn a Postgres instance on Docker just for the test
  def createPostgres(): PostgreSQLContainer[Nothing] = {
    val container: PostgreSQLContainer[Nothing] = PostgreSQLContainer("postgres")
      .withInitScript(sqlScript)
    container.start()
    container
  }

  // create a DataSource to connect to the Postgres instance
  def createDataSource(container: PostgreSQLContainer[Nothing]): DataSource = {
    val datasource = new PGSimpleDataSource
    datasource.setURL(container.getJdbcUrl)
    datasource.setUser(container.getUsername)
    datasource.setPassword(container.getPassword)
    datasource
  }
  // use the DataSource to create a Quill instance
  val dataSourceLayer: ZLayer[Any & Scope, Throwable, DataSource] = ZLayer {
    for {
      container <- ZIO.acquireRelease(ZIO.attempt(createPostgres()))(container =>
        ZIO.attempt(container.stop()).ignoreLogged
      )
      datasource <- ZIO.attempt(createDataSource(container))
    } yield datasource
  }

}
