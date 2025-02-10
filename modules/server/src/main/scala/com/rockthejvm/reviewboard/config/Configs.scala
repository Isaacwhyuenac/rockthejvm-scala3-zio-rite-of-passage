package com.rockthejvm.reviewboard.config

import com.typesafe.config.ConfigFactory
import zio.config.ConfigOps
import zio.{config, ConfigProvider, Tag, ZIO, ZLayer}
import zio.config.typesafe.FromConfigSourceTypesafe
import zio.config.magnolia.{deriveConfig, DeriveConfig}

object Configs {

  def makeConfig[T: {Tag, DeriveConfig}](
      filepath: String = "application.conf"
  )(propertyPath: String): ZIO[Any, Throwable, T] = {
    config.read(
      deriveConfig[T] from ConfigProvider.fromTypesafeConfig(ConfigFactory.load(filepath).getConfig(propertyPath))
    )
  }

  def makeConfigLayer[T: {Tag, DeriveConfig}](filepath: String = "application.conf")(propertyPath: String) =
    ZLayer.fromZIO(makeConfig[T](filepath)(propertyPath))

}
