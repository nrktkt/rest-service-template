package thepackage.models
import java.time.OffsetDateTime
import java.util.UUID

sealed trait AuthzContext

case object PublicAuthzContext extends AuthzContext

case class UserAuthzContext(userId: UUID, authenticatedAt: OffsetDateTime) extends AuthzContext
