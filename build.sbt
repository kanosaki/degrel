
import AssemblyKeys._

name := "degrel"

scalaVersion := "2.10.3"

resolvers += "Twitter Repository" at "http://maven.twitter.com"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.sonatypeRepo("public")

val scalazVersion = "7.0.4"

val akkaVersion = "2.2.3"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    "com.typesafe" %% "scalalogging-slf4j" % "1.0.1", 
    "org.rogach" %% "scallop" % "0.9.4",
    "org.scala-lang" % "jline" % "2.10.3",
    "commons-io" % "commons-io" % "2.4",
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "com.typesafe.akka" % "akka-actor_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-testkit_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-agent_2.10" % akkaVersion)

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
