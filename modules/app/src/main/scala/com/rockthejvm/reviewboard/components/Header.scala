package com.rockthejvm.reviewboard.components

import com.raquo.laminar.api.L.{`type`, a, alt, button, cls, div, href, htmlAttr, idAttr, img, li, navTag, span, src, ul, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import com.rockthejvm.reviewboard.common.Constants

object Header {
  def apply() = div(
    cls := "container-fluid p-0",
    div(
      cls := "jvm-nav",
      div(
        cls := "container",
        navTag(
          cls := "navbar navbar-expand-lg navbar-light JVM-nav",
          div(
            cls := "container",
            // TODO logo
            renderLogo(),
            button(
              cls                                         := "navbar-toggler",
              `type`                                      := "button",
              htmlAttr("data-bs-toggle", StringAsIsCodec) := "collapse",
              htmlAttr("data-bs-target", StringAsIsCodec) := "#navbarNav",
              htmlAttr("aria-controls", StringAsIsCodec)  := "navbarNav",
              htmlAttr("aria-expanded", StringAsIsCodec)  := "false",
              htmlAttr("aria-label", StringAsIsCodec)     := "Toggle navigation",
              span(cls := "navbar-toggler-icon")
            ),
            div(
              cls    := "collapse navbar-collapse",
              idAttr := "navbarNav",
              ul(
                cls := "navbar-nav ms-auto menu align-center expanded text-center SMN_effect-3",
                // TODO children
                renderNavLinks()
              )
            )
          )
        )
      )
    )
  )

  private def renderLogo() =
    a(
      href := "/",
      cls  := "navbar-brand",
      img(
        cls := "home-logo",
        src := Constants.logoImage,
        alt := "Rock the JVM logo"
      )
    )

  // list of <li> elements
  // Companies, Log In, Sign Up
  private def renderNavLinks() = List(
    renderNavLink("Companies", "/companies"),
    renderNavLink("Log In", "/login"),
    renderNavLink("Sign Up", "/signup")
  )

  private def renderNavLink(text: String, location: String) =
    li(
      cls := "nav-item",
      Anchors.renderNavLink(text, location, "nav-link jvm-item")
    )

}
