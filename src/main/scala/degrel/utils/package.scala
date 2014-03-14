package degrel

package object utils {
  val support = SupportUtils

  def runnable(f : () => Unit) = {
    new Runnable {
      override def run(): Unit = f()
    }
  }
}
