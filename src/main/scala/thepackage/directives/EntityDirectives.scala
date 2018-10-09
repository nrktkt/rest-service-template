package thepackage.directives
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.server.Directives._

trait EntityDirectives {

  def safeEntity[L, R](um: FromRequestUnmarshaller[Either[L, R]])(
    implicit errorMarshaller: ToResponseMarshaller[L]): Directive1[R] =
    entity(um)
      .flatMap {
        case Left(l)  => complete(l)
        case Right(r) => provide(r)
      }
}

object EntityDirectives extends EntityDirectives
