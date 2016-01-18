package degrel.cluster

import degrel.control.Interpreter
import degrel.control.cluster.ControllerFacade
import degrel.engine.Chassis

import scala.concurrent.Future

class ClusterInterpreter(override val chassis: Chassis, controller: ControllerFacade) extends Interpreter(chassis) {

  import controller.system.dispatcher

  override def startProcess(): Future[Unit] = controller.interpret(chassis.main.header.asCell) map { _ => () }
}
