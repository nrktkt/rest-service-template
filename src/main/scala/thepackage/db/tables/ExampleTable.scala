package thepackage.db.tables

import thepackage.db.PostgresProfile.api._
import thepackage.models.Example

class ExampleTable(tag: Tag) extends Table[Example](tag, "examples"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def field1 = column[Option[String]]("field_1")
  def field2 = column[String]("field_2")

  def * = (id.?, field1, field2) <> ((Example.apply _).tupled, Example.unapply)
}

object ExampleTable extends TableQuery(new ExampleTable(_))