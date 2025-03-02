package com.rockthejvm.reviewboard.demo

import com.rockthejvm.reviewboard.services.{EmailService, EmailServiceLive}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object EmailServiceDemo extends ZIOAppDefault {
  val program = for {
    emailService <- ZIO.service[EmailService]
    _ <- emailService.sendPasswordRecoveryEmail("spiderman@rockthejvm.com", "Hi from Rock the JVM!")
    _ <- zio.Console.printLine("Email Done!")
  } yield ()

  override def run: ZIO[Any & ZIOAppArgs & Scope, Any, Any] = {
    program.provideSomeLayer(EmailServiceLive.configuredLayer)
  }
}
