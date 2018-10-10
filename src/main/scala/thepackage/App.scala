package thepackage

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import thepackage.db.PostgresProfile
import thepackage.db.PostgresProfile.api._
import thepackage.routing.ExampleRoutes
import thepackage.services.ExampleService

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object App extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContext = system.dispatcher

  val dependencies = new ConcreteDependencies

  val routes = dependencies.exampleRoutes.routes

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}

class ConcreteDependencies(implicit ec: ExecutionContext) {
  val db = Database.forConfig("db")
  val exampleService = new ExampleService(db)
  val exampleRoutes = new ExampleRoutes(exampleService)
}
