package thepackage.models
import java.time.OffsetDateTime
import java.util.UUID

case class Token(
    token:     String,
    userId:    UUID,
    expiresAt: OffsetDateTime,
    createdAt: OffsetDateTime,
    deletedAt: Option[OffsetDateTime] = None
)
