package degrel

package object engine {
  lazy val default = {
    val engine = Engine()
    engine.boot()
    engine
  }
  val system = default.system
}
