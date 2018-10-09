package thepackage.routing

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import play.api.libs.json.JsError
import thepackage.directives.EntityDirectives._
import thepackage.payloads.requests.CreateExampleRequest
import thepackage.payloads.requests.CreateExampleRequest.format
import thepackage.payloads.responses.ExampleResponse
import thepackage.services.ExampleService
import thepackage.util._

import scala.concurrent.ExecutionContext

class ExampleRoutes(val exampleService: ExampleService)(implicit ec: ExecutionContext) {

  implicit val jsErrorTRM: ToResponseMarshaller[JsError] = ???

  val routes =
    // format: off
    pathPrefix("examples") (
      pathEnd (
        post (
          safeEntity(as[Either[JsError, CreateExampleRequest]]) apply ( createExampleRequest =>
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
