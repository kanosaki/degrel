package degrel.control.config

import java.net.InetAddress

import org.rogach.scallop.ScallopConf

class CLIArguments(args: Array[String]) extends ScallopConf(args) with ConfigBase {
  banner("DEGREL 0.0.1: Distributed Graph Rewriting system")

  private val patEndpoint = """(\w*):(\d*?)$""".r

  val consoleMode = opt[Boolean](name = "console")

  val enableWebFace = opt[String](name = "web", validate = {
    // port should be in tcp port range and valid address should be given.
    case patEndpoint(host, port) => try {
      port.toInt <= Short.MaxValue &&
      InetAddress.getByName(host) != null
    } catch {
      case _: Throwable => false
    }
  })

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
