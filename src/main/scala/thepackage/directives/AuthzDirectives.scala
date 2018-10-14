package thepackage.directives

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import thepackage.errors.InvalidAuthorizationError
import thepackage.models.{AuthzContext, PublicAuthzContext, UserAuthzContext}
import thepackage.services.AuthorizationService

trait AuthzDirectives {
  def extractAuthz: Directive1[AuthzContext]
  def requireUserAuthz: Directive1[UserAuthzContext]
}

class DefaultAuthzDirectives(authorizationService: AuthorizationService) extends AuthzDirectives {

  private val contextFromHeader = extractCredentials.flatMap[Tuple1[AuthzContext]] {
    case Some(OAuth2BearerToken(tokenString)) => onSuccess(authorizationService.retrieveValidToken(tokenString))
      .flatMap {
        case Some(token) => provide(UserAuthzContext(token.userId, token.authenticatedAt))
        case None => complete(InvalidAuthorizationError(
          "invalid_token",
          "The provided token either didn't exist or has expired"
        ))
      }
    case Some(credentials) => complete(InvalidAuthorizationError(
      "invalid_token",
      s"The ${credentials.scheme} authentication scheme is not supported"
    ))
    case None => provide(PublicAuthzContext)
  }

  val extractAuthz = optionalHeaderValueByType[AuthzContextHeader](()).flatMap {
    case Some(header) => provide(header.value)
    case None => contextFromHeader.flatMap(authzContext =>
      mapRequest(_.addHeader(new AuthzContextHeader(authzContext)))
        .tmap(_ => authzContext))
  }

  val requireUserAuthz = extractAuthz.flatMap {
    case user: UserAuthzContext => provide(user)
    case _ => complete(InvalidAuthorizationError(
      "invalid_token",
      "User authorization token required for this request."
    ))
  }
}

final class AuthzContextHeader(val context: AuthzContext) extends ModeledCustomHeader[AuthzContextHeader] {
  val companion = AuthzContextHeader
  def value = ???
  val renderInRequests = false
  val renderInResponses = false
}

object AuthzContextHeader extends ModeledCustomHeaderCompanion[AuthzContextHeader] {
  val name = "X-Authz-Context"
  def parse(value: String) = ???
}
