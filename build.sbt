name := "MetricsDemo"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies := Seq(
  "io.dropwizard.metrics" % "metrics-core" % "3.2.3",
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "org.json4s" %% "json4s-native" % "3.5.2",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.7"
)

val ac = (project in file("acceptanceTests"))
  .settings(
    scalaVersion := "2.12.2",
    libraryDependencies := Seq(
      "org.scalatest" %% "scalatest" % "3.0.1",
      "com.github.tomakehurst" % "wiremock" % "1.18",
      "com.typesafe.akka" %% "akka-http" % "10.0.9"
    )
  )
