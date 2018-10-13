package thepackage.models
import java.util.UUID

sealed trait AuthzContext

case object PublicAuthzContext extends AuthzContext

case class UserAuthzContext(userId: UUID) extends AuthzContext
