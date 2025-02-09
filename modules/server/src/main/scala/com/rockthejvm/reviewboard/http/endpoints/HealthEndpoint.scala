package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.{endpoint, plainBody, statusCode, stringBody, stringToPath, Endpoint}

trait HealthEndpoint extends BaseEndpoint {
  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("health")
    .description("Health check endpoint")
    .get
    .in("health")
    .out(plainBody[String])

  val errorEndpoint = baseEndpoint
    .tag("health")
    .name("error")
    .description("Error endpoint")
    .get
    .in("health" / "error")
    .out(plainBody[String])
}
