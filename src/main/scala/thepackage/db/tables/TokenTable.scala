package thepackage.db.tables

import java.time.OffsetDateTime
import java.util.UUID

import thepackage.db.PostgresProfile.api._
import thepackage.models.Token

class TokenTable(tag: Tag) extends Table[Token](tag, "tokens") {
  def token = column[String]("token", O.PrimaryKey)
  def userId = column[UUID]("user_id")
  def expiresAt = column[OffsetDateTime]("expires_at")
  def createdAt = column[OffsetDateTime]("created_at")
  def deletedAt = column[Option[OffsetDateTime]]("deleted_at")

  def * = (token, userId, expiresAt, createdAt, deletedAt) <> ((Token.apply _).tupled, Token.unapply)
}

object TokenTable extends TableQuery(new TokenTable(_))
