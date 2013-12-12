
name := "degrel"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers += "Twitter Repository" at "http://maven.twitter.com"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

val scalazVersion = "7.0.4"

val akkaVersion = "2.2.3"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1", 
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "com.typesafe.akka" % "akka-actor_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-testkit_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-agent_2.10" % akkaVersion)

initialCommands in console := "import scalaz._, Scalaz._"
