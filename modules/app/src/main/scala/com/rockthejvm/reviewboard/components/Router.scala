package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{cls, div, mainTag, given}
import com.rockthejvm.reviewboard.pages.{CompaniesPage, LoginPage, NotFoundPage, SignupPage}
import frontroute.{addNullaryDirectiveApply, elementToRoute, noneMatched, path, pathEnd, routes, stringToSegment}

object Router {
  def apply() =
    mainTag(
      routes(
        div(
          cls := "container-fluid",
          // potential children
          (pathEnd | path("companies")) { // localhost:1234 or localhost:1234/
            CompaniesPage()
          },
          path("login") {
            LoginPage()
          },
          path("signup") {
            SignupPage()
          },
          noneMatched {
            NotFoundPage()
          }
        )
      )
    )
}
