package com.rockthejvm.reviewboard.demo

import com.rockthejvm.reviewboard.utils.Hasher

object UserServiceDemo {
  def main(args: Array[String]): Unit = {
    val str = Hasher.hashPassword("rockthejvm")

    println(str)
  }
}
