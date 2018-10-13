package thepackage

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler}
import akka.stream.{ActorMaterializer, Materializer}
import com.fasterxml.jackson.core.JsonParseException
import com.typesafe.config.{Config, ConfigFactory}
import thepackage.db.PostgresProfile.api._
import thepackage.directives.DefaultAuthzDirectives
import thepackage.errors.Error._
import thepackage.errors.{JsonParsingError, RouteNotFoundError}
import thepackage.routing.{ExampleRoutes, UserRoutes}
import thepackage.services._
import thepackage.util._

import scala.concurrent.ExecutionContext

object App extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher

  val config = ConfigFactory.load

  val dependencies = new ConcreteDependencies(config)

  val bindingFuture = Http().bindAndHandle(dependencies.routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080")
}

class ConcreteDependencies(config: Config)(implicit ec: ExecutionContext, mat: Materializer) {
  implicit val clock = Clock.systemDefaultZone
  val db = Database.forConfig("db")

  val exampleService = new ExampleService(db)
  val exampleRoutes = new ExampleRoutes(exampleService)

  val passwordConfig = Argon2Config.fromConfig(config \ "services" \ "password")
  val passwordService = new Argon2PasswordService(passwordConfig)

  val authorizationServiceConfig = DefaultAuthorizationServiceConfig.fromConfig(config \ "services" \ "authorization")
  val authorizationService = new DefaultAuthorizationService(db)(authorizationServiceConfig)

  val authzDirectives = new DefaultAuthzDirectives(authorizationService)

  val userService = new DefaultUserService(db, passwordService)
  val userRoutes = new UserRoutes(userService, authorizationService, authzDirectives)

  val rejectionHandler = RejectionHandler.newBuilder
    .handle { case MalformedRequestContentRejection(_, e: JsonParseException) =>
      complete(JsonParsingError.fromException(e))
    }
    .handleNotFound(extractRequest { req =>
      onComplete(req.discardEntityBytes().future) { _ =>
        complete(RouteNotFoundError)
      }
    })
    .result
    .withFallback(RejectionHandler.default)

  val routes =
    handleRejections(rejectionHandler) (
      concat(
        exampleRoutes.routes,
        userRoutes.routes
      )
    )
}
