package degrel.engine

import java.util.concurrent.Executors

class BenchRewriter(poolSize: Int) {
  val pool = Executors.newFixedThreadPool(poolSize)

  def start() = {
  }

}
