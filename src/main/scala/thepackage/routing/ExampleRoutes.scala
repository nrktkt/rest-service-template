package thepackage.routing

import akka.http.scaladsl.marshalling.EmptyValue
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import thepackage.payloads.requests.CreateExampleRequest
import thepackage.payloads.requests.CreateExampleRequest.format
import thepackage.payloads.responses.ExampleResponse
import thepackage.services.ExampleService
import thepackage.util._

import scala.concurrent.ExecutionContext

class ExampleRoutes(val exampleService: ExampleService)(implicit ec: ExecutionContext) {

  val routes =
    // format: off
    pathPrefix("examples") (
      pathEnd (
        post (
          entity(as[CreateExampleRequest]) ( createExampleRequest =>
            complete(exampleService.createExample(createExampleRequest.toExample).map(ExampleResponse.fromExample))
          )
        )
      ) ~
      path(IntNumber) ( id =>
        get (
          onSuccess(exampleService.retrieveExample(id)) {
            case Some(example) => complete(ExampleResponse.fromExample(example))
            case None          => complete(StatusCodes.NotFound)
          }
        )
      )
    )
    // format: on
}
