package com.rockthejvm

import zio.{Console, Scope, Task, ZIO, ZIOAppArgs, ZIOAppDefault}
import zio.durationLong
import java.io.IOException

object ZIORecap extends ZIOAppDefault {

  // ZIO = data structure describing arbitrary computations (including side effects)\
  // "effects" = computations as values

  // basics
  val meaningfulLife: ZIO[Any, Nothing, Int] = ZIO.succeed(42)

  // fail
  val aFailure = ZIO.fail("something went wrong")

  // suspension/ delay
  val aSuspension: ZIO[Any, Throwable, Int] = ZIO.suspend(meaningfulLife)

  // map/ flatMap
  val improvedMOL: ZIO[Any, Nothing, Int] = meaningfulLife.map(_ * 2)
  val printingMOL = meaningfulLife.flatMap(value => Console.printLine(s"The meaning of life is $value"))

  private val smallProgram: ZIO[Any, IOException, Unit] = for {
    name <- Console.readLine("What's your name? ")
    _    <- Console.printLine(s"Welcome to ZIO, $name!")
  } yield ()

  // error handling
  val anAttempt: Task[Int] = ZIO.attempt {
    // expr which can throw
    Console.printLine("Trying something")
    val string: String = null
    string.length
  }

  // catching error effectfully
  val catchError = anAttempt.catchAll(_ => ZIO.succeed("Returning some different value"))
  val catchSelective = anAttempt.catchSome {
    case e: RuntimeException => ZIO.succeed(s"Ignoring runtime exceptions ${e}")
    case _                   => ZIO.succeed("Ignoring other exceptions")
  }

  // fibers

  val delayedValue = ZIO.sleep(1.second) *> zio.Random.nextIntBetween(0, 100)

  val aPair = for {
    _ <- delayedValue
    _ <- delayedValue
  } yield ()

  val aPairParallel = for {
    fiber1 <- delayedValue.fork // return some other effect which has a fiber
    fiber2 <- delayedValue.fork
    _      <- fiber1.join
    _      <- fiber2.join
  } yield () // this takes 1 second

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = smallProgram

}
