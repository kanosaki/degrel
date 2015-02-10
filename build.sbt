
import AssemblyKeys._

name := "degrel"

scalaVersion := "2.11.5"

//resolvers += "Twitter Repository" at "http://maven.twitter.com"

//resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.sonatypeRepo("public")

libraryDependencies ++= { 
  val scalazVersion = "7.1.0"
  val akkaVersion = "2.3.8"
  val sprayVersion = "1.3.2"
  val graphStreamVersion = "1.2"
  Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.3" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.rogach" %% "scallop" % "0.9.5",
    "jline" % "jline" % "2.12",
    "commons-io" % "commons-io" % "2.4",
//    "org.scalanlp" %% "breeze" % "0.8.1",
//    "org.scalanlp" %% "breeze-natives" % "0.8.1",
    "org.scalafx" %% "scalafx" % "8.0.20-R6",
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-testkit_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion,
    "com.typesafe.akka" % "akka-agent_2.11" % akkaVersion,
    "io.spray"  %%  "spray-can"     % sprayVersion,
    "io.spray"  %%  "spray-routing" % sprayVersion,
    "io.spray"  %%  "spray-testkit" % sprayVersion  % "test",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "org.scaldi" %% "scaldi" % "0.4",
    "org.scaldi" %% "scaldi-akka" % "0.4",
    "org.controlsfx" % "controlsfx" % "8.20.8",
    "com.github.monkeysintown" % "jgraphx" % "3.1.2.1",
    "org.graphstream" % "gs-core" % graphStreamVersion,
    "org.graphstream" % "gs-ui" % graphStreamVersion,
    "org.graphstream" % "gs-algo" % graphStreamVersion,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.5.1"
  )
}

scalacOptions ++= Seq("-feature", "-deprecation")

//scalacOptions in (Compile,doc) := Seq("-d doc/wiki/scaladoc")

initialCommands in console := "import scalaz._, Scalaz._"

assemblySettings


// scala.tools.jline関連のライブラリがassembly時にコンフリクトを起こすので
// 時前で競合解消
//mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
//  {
//    case PathList(ps @ _*) if ps.last == "libjansi.jnilib" => MergeStrategy.first
//    case PathList(ps @ _*) if ps.last == "jansi.dll" => MergeStrategy.first
//    case PathList(ps @ _*) if ps.last == "libjansi.so" => MergeStrategy.first
//    case PathList("org", "fusesource", xs @ _*) => MergeStrategy.first
//    case x => old(x)
//  }
//}
