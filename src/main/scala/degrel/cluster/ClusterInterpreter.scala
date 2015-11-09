package degrel.cluster

import degrel.control.Interpreter
import degrel.control.cluster.ControllerFacade
import degrel.engine.Chassis

class ClusterInterpreter(override val chassis: Chassis, controller: ControllerFacade) extends Interpreter(chassis) {
  override def startProcess(): Long = {
    controller.interpret(chassis.main.header.asCell)
    0
  }
}
