package degrel.control.cluster

import degrel.control.console.ConsoleHandle
import degrel.control.console.commands.ConsoleCommand
import degrel.engine.Chassis

class ClusterConsole(chassis: Chassis,
                     val cluster: ControllerFacade,
                     commandSet: Option[Seq[ConsoleCommand]] = None) extends ConsoleHandle(chassis, commandSet) {
  override protected def evalLine(line: String): Unit = {
    try {
      val input = degrel.parseVertex(line)
      this.current.send(input)
    } catch {
      case e: Throwable => console.println(e.toString)
    }
  }
}
