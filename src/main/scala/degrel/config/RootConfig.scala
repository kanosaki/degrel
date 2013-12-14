package degrel.config

class RootConfig extends Config {
  def parent: Config = null

  def get(key: Symbol): List[Any] = ???

  override def resolve(key: Symbol) = {
    this.get(key)
  }
}
