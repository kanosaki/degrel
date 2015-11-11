package degrel.cluster

import akka.util.Timeout
import scala.concurrent.duration._

object Timeouts {
  val immediate = Timeout(1.seconds)
  val short = Timeout(5.seconds)
  val middle = Timeout(30.seconds)

  val apiCall = short

  val infoGather = middle

  val long = Timeout(1.hours)
}
