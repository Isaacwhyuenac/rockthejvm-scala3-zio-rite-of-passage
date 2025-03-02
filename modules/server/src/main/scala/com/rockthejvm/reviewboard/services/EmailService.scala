package com.rockthejvm.reviewboard.services

import com.rockthejvm.reviewboard.config.{Configs, EmailServiceConfig, JWTConfig}
import zio.{Task, ZIO, ZLayer}

import java.util.Properties
import javax.mail.{Authenticator, PasswordAuthentication, Session}

trait EmailService {
  def sendEmail(to: String, subject: String, content: String): Task[Unit]

  def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit]
}

class EmailServiceLive private (emailConfig: EmailServiceConfig) extends EmailService {

  override def sendEmail(to: String, subject: String, content: String): Task[Unit] = {
    val messageZIO = for {
      props   <- propsResources
      session <- createSession(props)
      message <- createMessage(session)(emailConfig.sender, to, subject, content)
    } yield message

    messageZIO.map { message =>
      javax.mail.Transport.send(message)
    }

  }

  override def sendPasswordRecoveryEmail(to: String, token: String): Task[Unit] =
    val subject = "Password recovery"
    val content = s"Your password recovery token is: $token"
    sendEmail(to, subject, content)

  private val propsResources: Task[Properties] = {
    val props = new Properties()
    props.put("mail.smtp.auth", true)
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", emailConfig.host)
    props.put("mail.smtp.port", emailConfig.port)
    props.put("mail.smtp.ssl.trust", emailConfig.host)

    ZIO.succeed(props)
  }

  private def createSession(properties: Properties): Task[Session] =
    ZIO.attempt {
      Session.getInstance(
        properties,
        new Authenticator {
          override def getPasswordAuthentication: PasswordAuthentication =
            new PasswordAuthentication(emailConfig.username, emailConfig.password)
        }
      )
    }

  private def createMessage(
      session: Session
  )(from: String, to: String, subject: String, content: String): Task[javax.mail.Message] = ZIO.attempt {
    val message = new javax.mail.internet.MimeMessage(session)
    message.setFrom(from)
    message.setRecipients(javax.mail.Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setContent(content, "text/html; charset=utf-8")
    message
  }
}

object EmailServiceLive {
  val layer = ZLayer {
    for {
      emailConfig <- ZIO.service[EmailServiceConfig]
    } yield new EmailServiceLive(emailConfig)
  }

  val configuredLayer = Configs.makeConfigLayer[EmailServiceConfig]()("rockthejvm.email") >>> layer

}
