package degrel.config

class CLIArguments extends Config {
  def parent: Config = ???

  def get(key: Symbol): List[Any] = ???
}

object CLIArguments {
  def apply(args: Array[String]) = {

  }

  def get(key: Symbol): List[Any] = {
    ???
  }
}
