package degrel

package object engine {
  lazy val default = {
    val engine = new Engine("degrel")
    engine.boot()
    engine
  }
  val system = default.system
}
