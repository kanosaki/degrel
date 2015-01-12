package degrel.control.config

class RootConfig extends ConfigBase {
  def parent: ConfigBase = null

  override def resolve(key: Symbol) = {
    this.get(key)
  }

  def get(key: Symbol): List[Any] = ???
}
