package com.rockthejvm.reviewboard.http.endpoints

import sttp.tapir.{Endpoint, endpoint, plainBody, stringToPath}

trait HealthEndpoint {
  val healthEndpoint: Endpoint[Unit, Unit, Unit, String, Any] = endpoint
    .tag("health")
    .name("health")
    .description("Health check endpoint")
    .get
    .in("health")
    .out(plainBody[String])
}