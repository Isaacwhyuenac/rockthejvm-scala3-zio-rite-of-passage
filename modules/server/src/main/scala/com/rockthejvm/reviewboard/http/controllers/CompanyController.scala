package com.rockthejvm.reviewboard.http.controllers

import com.rockthejvm.reviewboard.domain.data.Company
import com.rockthejvm.reviewboard.http.endpoints.CompanyEndpoints
import com.rockthejvm.reviewboard.http.requests.CreateCompanyRequest
import sttp.tapir.server.ServerEndpoint
import zio.{Task, ZIO}

import scala.collection.mutable

class CompanyController private extends BaseController with CompanyEndpoints {

  val db: mutable.Map[Long, Company] = mutable.Map[Long, Company](
    -1L -> Company(-1, "Acme Inc.", "acme.com", "The best company in the world")
  )

  // create
  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogicSuccess { (req: CreateCompanyRequest) =>
    ZIO.succeed {
      val id         = db.keys.max + 1
      val newCompany = req.toCompany(id)
      db += (id -> newCompany)
      newCompany
    }
  }

  // getAll
  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogicSuccess { _ =>
    ZIO.succeed {
      db.values.toList
    }
  }

  // getById
  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogicSuccess { (id: String) =>
    ZIO
      .attempt(id.toLong)
      .map(db.get)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)

}

object CompanyController {
  val makeZIO: ZIO[Any, Nothing, CompanyController] = ZIO.succeed(new CompanyController)
}
