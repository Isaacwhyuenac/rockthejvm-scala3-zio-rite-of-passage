package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.{Company, UserID}
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import com.rockthejvm.reviewboard.services.{CompanyService, JWTService}
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

class CompanyController private (companyService: CompanyService, jwtService: JWTService)
    extends BaseController
    with CompanyEndpoints {

  // create
  val create: ServerEndpoint[Any, Task] = createEndpoint
    .serverSecurityLogic[UserID, Task](token => jwtService.verifyToken(token).either)
    .serverLogic { (_: UserID) => (req: CreateCompanyRequest) =>
      companyService.create(req).either
    }

  // getAll
  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogic { _ =>
    companyService.getAll().either
  }

  // getById
  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { (id: String) =>
    ZIO
      .attempt(id.toLong)
      .flatMap(companyService.getById)
      .catchSome { case _: NumberFormatException =>
        companyService.getBySlug(id)
      }
      .either
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)

}

object CompanyController {

  val makeZIO = for {
    companyService <- ZIO.service[CompanyService]
    jwtService     <- ZIO.service[JWTService]
  } yield new CompanyController(companyService, jwtService)

}
