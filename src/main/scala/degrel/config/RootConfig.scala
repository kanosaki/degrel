package degrel.config

class RootConfig extends ConfigBase {
  def parent: ConfigBase = null

  def get(key: Symbol): List[Any] = ???

  override def resolve(key: Symbol) = {
    this.get(key)
  }
}
