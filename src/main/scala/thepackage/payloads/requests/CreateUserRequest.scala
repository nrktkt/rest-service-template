package thepackage.payloads.requests
import play.api.libs.json.Json
import cats.data._
import cats.data.Validated._
import cats.implicits._
import thepackage.services.PasswordService

case class CreateUserRequest(email: String, name: String, password: String)

object CreateUserRequest {
  implicit val format = Json.format[CreateUserRequest]
}
