package thepackage.db.tables

import java.time.OffsetDateTime
import java.util.UUID

import thepackage.db.PostgresProfile.api._
import thepackage.models.User

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[UUID]("id", O.PrimaryKey)
  def email = column[String]("email")
  def name = column[String]("name")
  def password = column[String]("password")
  def createdAt = column[OffsetDateTime]("created_at")

  def * = (id, email, name, password, createdAt) <> ((User.apply _).tupled, User.unapply)
}

object UserTable extends TableQuery(new UserTable(_))
