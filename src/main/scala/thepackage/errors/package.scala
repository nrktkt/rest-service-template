package thepackage

import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.headers.{HttpChallenge, Location, `WWW-Authenticate`}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.Uri
import com.fasterxml.jackson.core.JsonParseException
import play.api.libs.json.{JsObject, Json, Writes}
import thepackage.util._

import collection.immutable.Seq

package object errors {

  sealed trait Error {
    def error: String = this.getClass.getSimpleName.stripSuffix("$")
    def message: String

    // todo there must be a better pattern here
    def toJson: JsObject = Json.obj("error" -> error, "message" -> message)
  }

  object Error {

    private implicit val writes = Writes[Error](_.toJson)

    implicit val toResponseMarshaller: ToResponseMarshaller[Error] = Marshaller.fromStatusCodeAndHeadersAndValue[Error].compose {
      case RouteNotFoundError                     => (NotFound, List(Location(Uri("https://http.cat/404"))), RouteNotFoundError)
      case invalidAuth: InvalidAuthorizationError => (Unauthorized, List(invalidAuth.challenge), invalidAuth)
      case error                                  => (BadRequest, Nil, error)
    }
  }

  sealed trait UserCreationError extends Error

  case class FieldValidationError(name: String, message: String)
  object FieldValidationError {
    implicit val format = Json.format[FieldValidationError]
  }

  case class RequestValidationError(fields: Seq[FieldValidationError]) extends UserCreationError {
    val message = "Request had invalid fields"

    override def toJson = super.toJson ++ Json.obj("fields" -> fields)
  }

  case class UserAlreadyExistsError(email: String) {
    val message = s"A user with the address $email already exists"
  }

  case class InvalidAuthorizationError(error6750: String, message: String) extends Error {
    val challenge = `WWW-Authenticate`(HttpChallenge(
      "Bearer",
      None,
      Map(
        "error" -> error6750,
        "error_description" -> message
      )
    ))
  }

  case object RouteNotFoundError extends Error {
    val message = "Not found"
  }

  case class JsonParsingError(message: String) extends Error
  object JsonParsingError {
    def fromException(exception: JsonParseException) = JsonParsingError(exception.getMessage)
  }
}
