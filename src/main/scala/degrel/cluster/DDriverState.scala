package degrel.cluster

import degrel.core.{DriverState, VertexPin}
import degrel.engine.Driver
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

trait DDriverState {
  def unpack(node: LocalNode, driver: Driver): DriverState

  def toJson: JObject
}

object DDriverState {

  import DriverState._

  case class DActive() extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = Active()

    override def toJson: JObject = {
      "type" -> "active"
    }
  }

  case class DPaused() extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = Paused()

    override def toJson: JObject = {
      "type" -> "paused"
    }
  }

  case class DFinished(returnTo: VertexPin, result: DGraph) extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = {
      Finished(returnTo, node.exchanger.unpack(result))
    }

    override def toJson: JObject = {
      ("type" -> "finished") ~
        ("return_to" -> returnTo.toSimpleJson) ~
        ("result" -> result.toJson)
    }
  }

  case class DStopping() extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = Stopping()

    override def toJson: JObject = {
      "type" -> "stopping"
    }
  }

  case class DStopped() extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = Stopped()

    override def toJson: JObject = {
      "type" -> "stopped"
    }
  }

  case class DDead(cause: Throwable) extends DDriverState {
    override def unpack(node: LocalNode, driver: Driver): DriverState = Dead(cause)

    override def toJson: JObject = {
      ("type" -> "dead") ~
        ("cause" -> (
          ("message" -> cause.getMessage) ~
            ("type" -> cause.getClass.toString) ~
            ("full_message" -> cause.toString)
          ))
    }
  }

  def pack(ds: DriverState, node: LocalNode, driver: Driver): DDriverState = {
    ds match {
      case Active() => DActive()
      case Paused() => DPaused()
      case Stopping() => DStopping()
      case Stopped() => DStopped()
      case Finished(pin, res) => {
        DFinished(pin, node.exchanger.packAll(res))
      }
      case Dead(cause) => DDead(cause)
    }
  }
}
