package com.rockthejvm.reviewboard.http.endpoints

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{endpoint, path, stringToPath}
import sttp.tapir.generic.auto.schemaForCaseClass

trait CompanyEndpoints {
  val createEndpoint = endpoint
    .tag("companies")
    .name("create")
    .description("Create a company")
    .in("companies")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = endpoint
    .tag("companies")
    .name("getAll")
    .description("Get all company listings")
    .in("companies")
    .get
    .out(jsonBody[List[Company]])

  val getByIdEndpoint = endpoint
    .tag("companies")
    .name("getById")
    .description("Get a company by ID")
    .in("companies" / path[String]("id"))
    .get
    .out(jsonBody[Option[Company]])
}
