package degrel.config

trait Config extends Serializable {
  def parent: Config

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
