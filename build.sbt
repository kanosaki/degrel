
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

//resolvers += "Twitter Repository" at "http://maven.twitter.com"

//resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.sonatypeRepo("public")

val akkaVersion = "2.4.0"

lazy val coreLibs = { 
  val scalazVersion = "7.1.3"
  val sprayVersion = "1.3.3"
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.github.scopt" %% "scopt" % "3.3.0",
    "jline" % "jline" % "2.12",
    "commons-io" % "commons-io" % "2.4",
//    "org.scalanlp" %% "breeze" % "0.8.1",
//    "org.scalanlp" %% "breeze-natives" % "0.8.1",
    "org.scalafx" %% "scalafx" % "8.0.20-R6",
    "org.scalaz" %% "scalaz-core" % scalazVersion,
    "org.scalaz" %% "scalaz-effect" % scalazVersion,
    "org.scalaz" %% "scalaz-typelevel" % scalazVersion,
    "org.scalaz" %% "scalaz-scalacheck-binding" % scalazVersion % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-agent" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "io.spray"  %%  "spray-can"     % sprayVersion,
    "io.spray"  %%  "spray-routing-shapeless2" % sprayVersion,
    "io.spray"  %%  "spray-testkit" % sprayVersion  % "test",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "org.scaldi" %% "scaldi" % "0.5.5",
    "org.scaldi" %% "scaldi-akka" % "0.5.5",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.5.1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.5.1",
    "org.parboiled" %% "parboiled" % "2.1.0",
    "com.opencsv" % "opencsv" % "3.3"
  )
}

lazy val testLibs = { 
  Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
  )
}

lazy val fxFrontLibs = { 
  val graphStreamVersion = "1.2"
  Seq(
    "org.controlsfx" % "controlsfx" % "8.20.8",
    "com.github.monkeysintown" % "jgraphx" % "3.1.2.1",
    "org.graphstream" % "gs-core" % graphStreamVersion,
    "org.graphstream" % "gs-ui" % graphStreamVersion,
    "org.graphstream" % "gs-algo" % graphStreamVersion
  )
}

lazy val commonSettings = Seq(
    organization := "in.teor",
    version := "0.1",
    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-deprecation")
  )

lazy val root = (project in file(".")).
  settings(SbtMultiJvm.multiJvmSettings).
  settings(commonSettings: _*).
  settings(
      name := "degrel",
      libraryDependencies ++= coreLibs ++ testLibs ++ fxFrontLibs,
      mainClass in assembly := Some("degrel.Main"),
      mainClass in Compile := Some("degrel.Main"),
      test in assembly := {},
      compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
      parallelExecution in Test := false,
      executeTests in Test <<= (executeTests in Test, executeTests in MultiJvm) map {
        case (testResults, multiNodeResults) => 
          val overall =
            if (testResults.overall.id < multiNodeResults.overall.id)
              multiNodeResults.overall
            else
              testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiNodeResults.events,
            testResults.summaries ++ multiNodeResults.summaries)
      }
    ).
  configs(MultiJvm)


initialCommands in console := "import scalaz._, Scalaz._"

//scalacOptions in (Compile,doc) := Seq("-d doc/wiki/scaladoc")


//assemblySettings

// for windows
javacOptions in compile ++= Seq("-encoding", "UTF-8")

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
