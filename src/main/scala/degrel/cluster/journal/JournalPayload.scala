package degrel.cluster.journal

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.ActorPath
import degrel.cluster.{DDriverState, DGraph}
import degrel.core.{ID, NodeID, VertexPin}
import org.apache.commons.lang3.StringUtils
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._

case class JournalPayload(actor: ActorPath, origin: NodeID, uid: Long, nodeTs: Calendar, nodeTick: Long, item: Journal) {
  def toJson: JObject = {
    ("type" -> "payload") ~
      ("actor" -> actor.toString) ~
      ("origin" -> origin) ~
      ("nodeTimestamp" -> Journal.timestampFormat.format(nodeTs.getTime)) ~
      ("nodeTick" -> nodeTick) ~
      ("uid" -> StringUtils.leftPad(BigInt(uid).toString(36), 13, '0')) ~
      ("content" -> item.toJson)
  }
}

sealed trait Journal {
  def repr: String

  def toJson: JObject
}

object Journal {
  val timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

  case class Info(msg: String) extends Journal {
    override def repr: String = {
      msg
    }

    override def toJson: JObject = {
      ("type" -> "info") ~
        ("message" -> msg)
    }
  }

  case class CellSpawn(prototypeCell: ID, cell: DGraph, returnTo: VertexPin, spawnAt: NodeID) extends Journal {
    override def repr: String = {
      s"SPAWN $prototypeCell at $spawnAt"
    }

    override def toJson: JObject = {
      ("type" -> "spawn") ~
        ("at" -> spawnAt) ~
        ("prototype" -> prototypeCell.toString) ~
        ("cell" -> cell.toJson) ~
        ("return_to" -> Option(returnTo).map(_.toSimpleJson).orNull) // TODO: serialize as Pin
    }
  }

  case class Load(at: NodeID, graph: DGraph, name: String) extends Journal {
    override def repr: String = s"Loading $name"

    override def toJson: JObject = {
      ("type" -> "load") ~
        ("at" -> at) ~
        ("name" -> name) ~
        ("graph" -> graph.toJson)
    }

  }

  case class DriverStateUpdate(id: ID, prev: DDriverState, next: DDriverState) extends Journal {
    override def repr: String = {
      s"DRIVER_STATE_UPDATE ID: $id | $prev --> $next"
    }

    override def toJson: JObject = {
      ("type" -> "driver_state_update") ~
        ("id" -> id.toString) ~
        ("prev" -> prev.toJson) ~
        ("next" -> next.toJson)
    }
  }

  case class Write(on: ID, to: ID, value: DGraph) extends Journal {
    override def repr: String = {
      s"WRITE on: $on to: $to value: ${value.pp}"
    }

    override def toJson: JObject = {
      ("type" -> "write") ~
        ("on" -> on.toString) ~
        ("to" -> to.toString) ~
        ("value" -> value.toJson)
    }
  }

  case class SessionFinished(elapsedMs: Long) extends Journal {
    override def repr: String = {
      s"FINISHED elapsed: $elapsedMs(ms)"
    }

    override def toJson: JObject = {
      ("type" -> "session_finished") ~
        ("elapsed_ms" -> elapsedMs)
    }
  }

  object Filters {
    val none = new WhiteListJournalFilter()

    val important = new WhiteListJournalFilter()
      .accept[DriverStateUpdate]
      .accept[CellSpawn]
      .accept[Info]

    val all = new ThroughJournalFilter()
  }

}


class JournalPrinter(items: Iterable[JournalPayload]) {
  def printTo(out: java.io.PrintStream) = {

  }
}

object JournalPrinter {
  def apply(items: Iterable[JournalPayload]) = new JournalPrinter(items)
}

