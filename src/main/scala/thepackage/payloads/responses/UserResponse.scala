package thepackage.payloads.responses
import java.time.OffsetDateTime
import java.util.UUID

import play.api.libs.json.Json
import thepackage.models.User

case class UserResponse(
    id:        UUID,
    email:     String,
    name:      String,
    createdAt: OffsetDateTime
)
object UserResponse {
  implicit val format = Json.format[UserResponse]

  def fromUser(user: User) = UserResponse(
    user.id,
    user.email,
    user.name,
    user.createdAt
  )
}
