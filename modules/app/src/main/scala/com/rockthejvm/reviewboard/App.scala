package com.rockthejvm.reviewboard

import com.raquo.laminar.api.L.{div, render}
import com.rockthejvm.reviewboard.components.{Header, Router}
import frontroute.LinkHandler
import org.scalajs.dom.document

object App {

  val app = div(
    Header(),
    Router()
  ).amend(LinkHandler.bind) // handle internal links

  def main(args: Array[String]): Unit = {
    val containerNode = document.getElementById("app")
    render(
      containerNode,
//      Tutorial.staticContent
      app
//      Tutorial.timeUpdated
    )
  }
}
