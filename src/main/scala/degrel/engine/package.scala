package degrel

package object engine {
  val default = {
    val engine = new Engine("degrel")
    engine.boot()
    engine
  }
  val system = default.system
}
