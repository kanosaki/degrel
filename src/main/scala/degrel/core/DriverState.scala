package degrel.core

/**
  * Active <--> Pause
  * (Active|Pause) --> Finished
  * (Active|Pause) --> Dead
  */
trait DriverState {
  def isStopped: Boolean
}

object DriverState {

  /**
    * The driver is working.
    * Rewritings is in progress.
    */
  case class Active() extends DriverState {
    override def isStopped: Boolean = false
  }

  /**
    * The driver is not Active. But no fin have been detected.
    * It will be back to Active when a new graph is delivered.
    */
  case class Paused(steps: Long) extends DriverState {
    override def isStopped: Boolean = false
  }

  /**
    * The driver is not active.
    * Because fin statement is detected.
    * So sending messages to this driver causes nothing.
    */
  case class Finished(returnTo: VertexPin, result: Vertex) extends DriverState {
    override def isStopped: Boolean = true
  }

  /**
    * Child drivers are stopped.
    * But effects from child drivers haven't been checked.
    * So waiting for re-check.
    */
  case class Stopping() extends DriverState {
    override def isStopped: Boolean = false
  }

  /**
    * Stopped by automatic halting.
    * (When the system detects that this cell never will be active in future,
    * it will be automatically mark the cell as Stopped.)
    */
  case class Stopped() extends DriverState {
    override def isStopped: Boolean = true
  }

  /**
    * A exception have been risen in the Driver.
    * It is stopped, and never will resume.
    */
  case class Dead(cause: Throwable) extends DriverState {
    override def isStopped: Boolean = true
  }

}
