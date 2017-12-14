package thepackage.payloads.requests

import play.api.libs.json.Json
import thepackage.models.Example

case class CreateExampleRequest(field1: Option[String], field2: String) {
  def toExample = Example(None, field1, field2)
}

object CreateExampleRequest {
  implicit val format = Json.format[CreateExampleRequest]
}
