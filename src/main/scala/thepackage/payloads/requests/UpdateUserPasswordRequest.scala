package thepackage.payloads.requests
import play.api.libs.json.Json

case class UpdateUserPasswordRequest(password: String)
object UpdateUserPasswordRequest {
  implicit val format = Json.format[UpdateUserPasswordRequest]
}
