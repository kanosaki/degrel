package degrel.config

trait ConfigBase extends Serializable {
  def parent: ConfigBase

  def get(key: Symbol): List[Any]

  /**
   * Resolve order: CLIArguments -> this -> parent -> parent.parent -> ... -> RootConfig
   * @param key
   * @return
   */
  def resolve(key: Symbol) = {
    CLIArguments.get(key) ++ parent.resolveTree(key)
  }

  protected def resolveTree(key: Symbol): List[Any] = {
    this.get(key) ++ parent.resolveTree(key)
  }
}
