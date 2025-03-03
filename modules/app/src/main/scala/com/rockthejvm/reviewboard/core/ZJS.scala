package com.rockthejvm.reviewboard.core

import com.raquo.airstream.eventbus.EventBus
import sttp.tapir.Endpoint
import zio.{Runtime, Task, Unsafe, ZIO}

object ZJS {

  def useBackend[A](clientFun: BackendClient => Task[A]): ZIO[BackendClient, Throwable, A] =
    ZIO.serviceWithZIO[BackendClient](clientFun)

  extension [E <: Throwable, A](currentZIO: ZIO[BackendClient, E, A]) {
    def emitTo(eventBus: EventBus[A]) =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(
          currentZIO.tap(value => ZIO.attempt(eventBus.emit(value))).provide(BackendClientLive.configuredLayer)
        )
      }
  }

  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any]) {
    def apply(payload: I): Task[O] =
      (for {
        backendClient <- ZIO.service[BackendClient]
        response      <- backendClient.endpointRequestZIO(endpoint)(payload)
      } yield response)
        .provide(BackendClientLive.configuredLayer)
  }
}
