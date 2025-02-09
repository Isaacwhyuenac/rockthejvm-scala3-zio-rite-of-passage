package com.rockthejvm.reviewboard.demo

import com.rockthejvm.reviewboard.utils.Hasher

object UserServiceDemo {
  def main(args: Array[String]): Unit = {
    val str = Hasher.generateHash("rockthejvm")

    println(str)

    ClipboardModule.Clipboard.writeString(str)

    Hasher.validateHash("rockthejvm", str) match {
      case true  => println("Match")
      case false => println("No match")
    }
  }
}
