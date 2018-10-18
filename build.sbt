import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success, Try}

name := "akka-http-slick"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11",

  "org.typelevel" %% "cats-core" % "1.4.0",

  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",

  "com.github.tminglei" %% "slick-pg" % "0.15.4",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.15.4",

  "com.typesafe.play" %% "play-json" % "2.6.8",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "org.slf4j" % "slf4j-simple" % "1.7.25",

  "de.mkammerer" % "argon2-jvm" % "2.5"
)

scalacOptions += "-Ypartial-unification"

//region flyway
enablePlugins(FlywayPlugin)

libraryDependencies += "org.flywaydb" % "flyway-core" % "5.0.7" % Compile
val resourceDir = new File("src/main/resources")
Try {
  val dbConf = ConfigFactory
    .load(ConfigFactory.parseFile(resourceDir / "db.conf"))
    .getConfig("db.properties")
  
  (
    dbConf.getString("url"),
    dbConf.getString("user"),
    dbConf.getString("password")
  )
} match {
  case Success((url, user, password)) =>
    flywayLocations += "db/migration"
    flywayUrl := url
    flywayUser := user
    flywayPassword := password
  case Failure(e) =>
    println(s"Failed to load flyway configuration: ${e.getMessage}") // todo warn level in logger
    flywayLocations += "db/migration"
}
//endregion
