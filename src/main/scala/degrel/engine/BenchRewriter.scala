package degrel.engine

import java.util.concurrent.{TimeUnit, ThreadPoolExecutor, Executors}

class BenchRewriter(poolSize: Int) {
  val pool = Executors.newFixedThreadPool(poolSize)

  def start() = {
  }

}
