package thepackage.payloads.responses

import play.api.libs.json.Json
import thepackage.models.Example

case class ExampleResponse(id: Int, field1: Option[String], field2: String)
object ExampleResponse {
  implicit val format = Json.format[ExampleResponse]

  def fromExample(example: Example) = ExampleResponse(
    example.id.get,
    example.field1,
    example.field2
  )
}