package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{Endpoint, endpoint, path, stringToPath}
import sttp.tapir.generic.auto.schemaForCaseClass

trait CompanyEndpoints extends BaseEndpoint {
  val createEndpoint = baseEndpoint
    .tag("companies")
    .name("create")
    .description("Create a company")
    .in("companies")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = baseEndpoint
    .tag("companies")
    .name("getAll")
    .description("Get all company listings")
    .in("companies")
    .get
    .out(jsonBody[List[Company]])

  val getByIdEndpoint = baseEndpoint
    .tag("companies")
    .name("getById")
    .description("Get a company by ID")
    .in("companies" / path[String]("id"))
    .get
    .out(jsonBody[Option[Company]])
}
