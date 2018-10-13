package thepackage.services

import thepackage.db.PostgresProfile.api._
import thepackage.db.PostgresProfile.backend.Database
import thepackage.db.tables.ExampleTable
import thepackage.models.Example

import scala.concurrent.ExecutionContext

class ExampleService(val db: Database)(implicit ec: ExecutionContext) {

  def retrieveExample(id: Int) =
    db.run(ExampleTable.filter(_.id === id).result.headOption)

  def createExample(example: Example) =
    db.run((ExampleTable returning ExampleTable.map(_.id)) += example)
      .map(id => example.copy(id = Some(id)))
}
