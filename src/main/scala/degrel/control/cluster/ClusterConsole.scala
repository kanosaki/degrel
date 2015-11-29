package degrel.control.cluster

import degrel.control.console.ConsoleHandle
import degrel.control.console.commands.ConsoleCommand
import degrel.engine.Chassis

import scala.concurrent.Await
import scala.concurrent.duration._

class ClusterConsole(chassis: Chassis,
                     val cluster: ControllerFacade,
                     commandSet: Option[Seq[ConsoleCommand]] = None) extends ConsoleHandle(chassis, commandSet) {
  override protected def evalLine(line: String): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    try {
      val input = degrel.parseVertex(line)
      Await.result(this.current.send(input), 10.seconds)
    } catch {
      case e: Throwable => console.println(e.toString)
    }
  }
}
