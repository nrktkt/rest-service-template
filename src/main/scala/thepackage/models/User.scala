package thepackage.models
import java.time.OffsetDateTime
import java.util.UUID

case class User(
    id:        UUID,
    email:     String,
    name:      String,
    password:  String,
    createdAt: OffsetDateTime
)
