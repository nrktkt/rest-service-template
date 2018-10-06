name := "akka-http-slick"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.11",

  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",

  "com.github.tminglei" %% "slick-pg" % "0.15.4",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.15.4",

  "com.typesafe.play" %% "play-json" % "2.6.8",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)

scalafmtOnCompile := true
