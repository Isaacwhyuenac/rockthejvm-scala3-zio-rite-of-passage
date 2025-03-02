package com.rockthejvm.reviewboard.core

import com.raquo.airstream.eventbus.EventBus
import com.rockthejvm.reviewboard.config.BackendClientConfig
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.{SttpBackend, UriContext}
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.{Runtime, Task, Unsafe, ZIO}

object ZJS {
  val backend: SttpBackend[Task, ZioStreams & capabilities.WebSockets] = FetchZioBackend()
  val interpreter: SttpClientInterpreter                               = SttpClientInterpreter()
  val backendClient: BackendClientLive = new BackendClientLive(
    backend,
    interpreter,
    BackendClientConfig(Some(uri"http://localhost:8080"))
  )

  def backendCall[A](clientFun: BackendClient => Task[A]): Task[A] =
    clientFun(backendClient)

  extension [E <: Throwable, A](currentZIO: ZIO[Any, E, A])
    def emitTo(eventbus: EventBus[A]) =
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(
          currentZIO.tap(value => ZIO.attempt(eventbus.emit(value)))
        )
      }

  extension [I, E <: Throwable, O](endpoint: Endpoint[Unit, I, E, O, Any])
    def apply(payload: I): Task[O] =
      backendClient.endpointRequestZIO(endpoint)(payload)

}
