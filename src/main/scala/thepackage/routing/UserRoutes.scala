package thepackage.routing

import java.time.Clock

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{CacheDirectives, `Cache-Control`}
import akka.http.scaladsl.server.Directives._
import cats.implicits._
import play.api.libs.json.Json
import thepackage.directives.AuthzDirectives
import thepackage.payloads.requests.{CreateUserRequest, UpdateUserPasswordRequest}
import thepackage.payloads.responses.{SuccessfulAuthorizationResponse, UserResponse}
import thepackage.services.{AuthorizationService, UserService}
import thepackage.util._
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers._

import scala.concurrent.ExecutionContext

class UserRoutes(userService: UserService, authorizationService: AuthorizationService, authzDirectives: AuthzDirectives)
  (implicit ec: ExecutionContext, clock: Clock) {
  import authzDirectives._

  val routes =
    // format: OFF
    pathPrefix("users") (
      pathEnd (
        post (
          entity(as[CreateUserRequest]) (request =>
            complete(
              userService
                .createUser(request).et
                .map(Created -> UserResponse.fromUser(_))
            ))
        )
      ) ~
      requireUserAuthz ( authContext =>
        pathPrefix("me") (
          pathEnd (
            get (
              complete(
                userService
                  .retrieveUser(authContext.userId)
                  .map(maybeUser => UserResponse.fromUser(maybeUser.get))
              )
            )
          ) ~
          path("password") (
            put (
              entity(as[UpdateUserPasswordRequest]) ( request =>
                complete(
                  userService.updateUserPassword(authContext.userId, request.password)
                    .map(_ => NoContent)
                )
              )
            )
          )
        )
      )
    ) ~
    path("token") (
      post (
        formFields("grant_type" ! "password", "username", "password") ((username, password) =>
          onSuccess(userService.retrieveUserByCredentials(username, password)) {
            case None => complete(BadRequest -> Json.obj("error" -> "invalid_grant"))
            case Some(user) => onSuccess(authorizationService.createToken(user.id)) (token =>
              complete((
                OK,
                List(`Cache-Control`(List(CacheDirectives.`no-store`))),
                SuccessfulAuthorizationResponse.fromToken(token)
              )))
          }) ~
          formField("grant_type" ? "none") (grantType =>
            complete(BadRequest -> Json.obj(
              "error" -> "unsupported_grant_type",
              "error_description" -> s"grant type $grantType not supported"
            )))
      )
    )
  // format: ON
}
