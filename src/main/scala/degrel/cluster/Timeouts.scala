package degrel.cluster

import akka.util.Timeout
import scala.concurrent.duration._

object Timeouts {
  val short = Timeout(5.seconds)
  val middle = Timeout(30.seconds)

  val apiCall = short

  val infoGather = middle
}
