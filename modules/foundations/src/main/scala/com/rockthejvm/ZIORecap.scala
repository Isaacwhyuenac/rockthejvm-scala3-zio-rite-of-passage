package com.rockthejvm

import zio.{Console, Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object ZIORecap extends ZIOAppDefault {

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = Console.printLine("rock the jvm")
  
}
