
name := "degrel"

scalaVersion := "2.10.3"

resolvers += "Twitter Repository" at "http://maven.twitter.com"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1"
    )
