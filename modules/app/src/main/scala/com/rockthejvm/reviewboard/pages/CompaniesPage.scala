package com.rockthejvm.reviewboard.pages

import com.raquo.laminar.api.L.{*, given}
import com.rockthejvm.reviewboard.common.Constants
import com.rockthejvm.reviewboard.components.Anchors
import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.core.ZJS.{apply, emitTo, useBackend}

object CompaniesPage {

  val dummyCompany = Company(
    1L,
    "dummy-company",
    "Dummy company",
    "https://dummy.com",
    Some("Anywhere"),
    Some("On Mars"),
    Some("space travel"),
    None,
    List("space", "scala")
  )

  private val companiesBus = new EventBus[List[Company]]

  def performBackendCall(): Unit = {

    // run the ZIO effect
    val companiesZIO = useBackend(_.company.getAllEndpoint(()))
    companiesZIO.emitTo(companiesBus)
  }

  def apply() =
    sectionTag(
      onMountCallback(_ => performBackendCall()),
      cls := "section-1",
      div(
        cls := "container company-list-hero",
        h1(
          cls := "company-list-title",
          "Rock the JVM Companies Board"
        )
      ),
      div(
        cls := "container",
        div(
          cls := "row jvm-recent-companies-body",
          div(
            cls := "col-lg-4",
            div("TODO filter panel here")
          ),
          div(
            cls := "col-lg-8",
            children <-- companiesBus.events.map(
              _.map(renderCompany(_))
            )
          )
        )
      )
    )

  private def renderCompanyPicture(company: Company) =
    img(
      cls := "img-fluid",
      src := company.image.getOrElse(Constants.companyLogoPlaceholder),
      alt := company.name
    )

  private def renderDetail(icon: String, value: String) =
    div(
      cls := "company-detail",
      i(cls := s"fa fa-$icon company-detail-icon"),
      p(
        cls := "company-detail-value",
        value
      )
    )

  private def fullLocationString(company: Company): String =
    (company.location, company.country) match {
      case (Some(loc), Some(country)) => s"$loc, $country"
      case (Some(loc), None)          => loc
      case (None, Some(country))      => country
      case (None, None)               => "N/A"
    }

  private def renderOverview(company: Company) =
    div(
      cls := "company-summary",
      renderDetail("location-dot", fullLocationString(company)),
      renderDetail("tags", company.tags.mkString(", "))
    )

  private def renderAction(company: Company) =
    div(
      cls := "jvm-recent-companies-card-btn-apply",
      a(
        href   := company.url,
        target := "blank",
        button(
          `type` := "button",
          cls    := "btn btn-danger rock-action-btn",
          "Website"
        )
      )
    )

  def renderCompany(company: Company) =
    div(
      cls := "jvm-recent-companies-cards",
      div(
        cls := "jvm-recent-companies-card-img",
        renderCompanyPicture(company)
      ),
      div(
        cls := "jvm-recent-companies-card-contents",
        h5(
          Anchors.renderNavLink(
            company.name,
            s"/company/${company.id}",
            "company-title-link"
          )
        ),
        renderOverview(company)
      ),
      renderAction(company)
    )
}
