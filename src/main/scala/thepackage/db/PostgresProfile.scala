package thepackage.db

import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

trait PostgresProfile extends ExPostgresProfile with PgArraySupport with PgDate2Support with PgPlayJsonSupport {

  // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"
  def pgjson = "jsonb"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api = PostgresAPI

  object PostgresAPI extends API with ArrayImplicits with DateTimeImplicits with JsonImplicits {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper = new AdvancedArrayJdbcType[JsValue](
      pgjson,
      (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
      (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
    ).to(_.toList)
  }
}

object PostgresProfile extends PostgresProfile
