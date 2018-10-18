package thepackage

import java.time.{Clock, OffsetDateTime}

import akka.http.scaladsl.marshalling.{Marshaller, PredefinedToEntityMarshallers, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromRequestUnmarshaller, PredefinedFromEntityUnmarshallers, Unmarshaller}
import cats.data.{EitherT, Validated}
import com.typesafe.config.Config
import play.api.libs.json._

import scala.concurrent.Future
import scala.language.higherKinds

package object util {

  implicit val jsonMarshaller: ToEntityMarshaller[JsValue] = PredefinedToEntityMarshallers
    .byteArrayMarshaller(ContentTypes.`application/json`)
    .compose[JsValue](Json.toBytes)

  implicit def writesEntityMarshaller[A](implicit writes: Writes[A]): ToEntityMarshaller[A] =
    jsonMarshaller.compose[A](writes.writes)

  implicit def readsUnmarshaller[A](implicit reads: Reads[A]) =
    Unmarshaller[JsValue, A](_ => js =>
      reads.reads(js) match {
        case JsSuccess(value, _) => Future.successful(value)
        case error: JsError      => Future.failed(JsResult.Exception(error))
      })

  implicit def readsEntityUnmarshaller[A](implicit unm: Unmarshaller[JsValue, A]): FromEntityUnmarshaller[A] =
    jsonUnmarshaller.andThen(unm)

  implicit def requestToEntUnm[A](implicit unm: FromEntityUnmarshaller[A]): FromRequestUnmarshaller[A] =
    Unmarshaller.strict[HttpRequest, HttpEntity](_.entity).andThen(unm)

  implicit def eitherTMarshaller[F[_], A, B, T](implicit marshaller: Marshaller[F[Either[A, B]], T]): Marshaller[EitherT[F, A, B], T] =
    marshaller.compose[EitherT[F, A, B]](_.value)

  // todo implement exception handler for json parsing failure
  implicit val jsonUnmarshaller: FromEntityUnmarshaller[JsValue] =
    PredefinedFromEntityUnmarshallers.byteArrayUnmarshaller
      .map(Json.parse)
      .forContentTypes(ContentTypes.`application/json`)

  trait Validatable[A, B, C] {
    def validate(a: A): Validated[B, C]
  }

  implicit class ValidatableSyntax[A](val a: A) extends AnyVal {
    def validate[B, C](implicit validatable: Validatable[A, B, C]) = validatable.validate(a)
  }

  def now(implicit clock: Clock) = OffsetDateTime.now(clock)

  implicit class EitherTSyntax[F[_], A, B](val value: F[Either[A, B]]) extends AnyVal {
    def et = EitherT(value)
  }

  implicit class ConfigExtensions(val config: Config) extends AnyVal {
    def \(s: String) = config.getConfig(s)
  }

}
