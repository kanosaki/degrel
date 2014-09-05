package degrel.config

import org.rogach.scallop.ScallopConf

class CLIArguments(args: Array[String]) extends ScallopConf(args) with ConfigBase {
  banner("DEGREL 0.0.1: Distributed Graph Rewriting system")

  val consoleMode = opt[Boolean](name = "console")

  val inputFile = trailArg[String](name = "input")

  val loggingMode = opt[String]()


  def parent: ConfigBase = ???

  def get(key: Symbol): List[Any] = ???
}

object CLIArguments {
  def apply(args: Array[String]) = {

  }

  def get(key: Symbol): List[Any] = {
    ???
  }
}
