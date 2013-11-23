
name := "degrel"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature")

resolvers += "Twitter Repository" at "http://maven.twitter.com"

val scalazVersion = "7.0.4"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1", 
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test"
    )

initialCommands in console := "import scalaz._, Scalaz._"
