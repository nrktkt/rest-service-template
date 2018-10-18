package thepackage.payloads.responses
import java.time.{Clock, Duration}

import play.api.libs.json.Json
import thepackage.models.Token
import thepackage.util.now

case class SuccessfulAuthorizationResponse(access_token: String, token_type: String, expires_in: Long)

object SuccessfulAuthorizationResponse {
  implicit val format = Json.format[SuccessfulAuthorizationResponse]

  def fromToken(token: Token)(implicit clock: Clock) = SuccessfulAuthorizationResponse(
    token.token,
    "Bearer",
    Duration.between(token.expiresAt, now).getSeconds
  )
}
