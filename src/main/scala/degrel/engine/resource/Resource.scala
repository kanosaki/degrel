package degrel.engine.resource

/**
 * Resource set for degrel engine.
 */
trait Resource {
  def console: Console
}

class DefaultResource extends Resource {
  override val console: Console = new DefaultConsole()
}
