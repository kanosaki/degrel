package degrel

package object engine {
  lazy val default = {
    val engine = new Chassis("degrel")
    engine.boot()
    engine
  }
  val system = default.system
}
