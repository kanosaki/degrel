package degrel.engine.sphere

/**
 * Resource set for degrel engine.
 */
trait Sphere {
  var console: Console
}

class DefaultSphere extends Sphere {
  override var console: Console = new DefaultConsole()
}
