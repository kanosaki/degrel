
import AssemblyKeys._

name := "degrel"

scalaVersion := "2.11.1"

//resolvers += "Twitter Repository" at "http://maven.twitter.com"

//resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.sonatypeRepo("public")

val scalazVersion = "7.0.6"

val akkaVersion = "2.3.3"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.1.1" % "test",
    "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
    "org.rogach" %% "scallop" % "0.9.5",
    "org.scala-lang" % "jline" % "2.10.3",
    "commons-io" % "commons-io" % "2.4",
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-agent_2.11" % akkaVersion)

scalacOptions ++= Seq("-feature", "-deprecation")

scalacOptions in (Compile,doc) := Seq("-d doc/wiki/scaladoc")

initialCommands in console := "import scalaz._, Scalaz._"

assemblySettings


// scala.tools.jline関連のライブラリがassembly時にコンフリクトを起こすので
// 時前で競合解消
mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList(ps @ _*) if ps.last == "libjansi.jnilib" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last == "jansi.dll" => MergeStrategy.first
    case PathList("org", "fusesource", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}
