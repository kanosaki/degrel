package degrel.cluster

import java.util.Calendar

import akka.actor.ActorRef
import degrel.core.{DriverState, ID, NodeID}

case class JournalPayload(acotr: ActorRef, origin: NodeID, nodeTs: Calendar, nodeTick: Long, item: Journal) {

}

sealed trait Journal {
  def repr: String
}

object Journal {

  case class Info(msg: String) extends Journal {
    override def repr: String = {
      msg
    }
  }

  case class CellSpawn(prototypeCell: ID, spawnAt: NodeID) extends Journal {
    override def repr: String = {
      s"SPAWN $prototypeCell at $spawnAt"
    }
  }

  case class DriverStateUpdate(id: ID, prev: DriverState, next: DriverState) extends Journal {
    override def repr: String = {
      s"DRIVER_STATE_UPDATE ID: $id | $prev --> $next"
    }
  }

  case class Write(on: ID, to: ID, value: DGraph) extends Journal {
    override def repr: String = {
      s"WRITE on: $on to: $to value: ${value.pp}"
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

