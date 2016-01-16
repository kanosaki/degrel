package degrel.engine

trait DriverOp {

}

object DriverOp {
  case class Write()
  case class AddRoot()
  case class RemoveRoot()
  case class Stop()
}
