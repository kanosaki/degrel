package degrel.cluster

import java.io.PrintStream
import java.util.Calendar

import akka.actor.ActorRef
import degrel.core.{NodeID, ID}

case class JournalPayload(acotr: ActorRef, origin: NodeID, nodeTs: Calendar, nodeTick: Long, item: Journal) {

}

sealed trait Journal {
  def print(out: PrintStream)
}

object Journal {

  case class Info(msg: String) extends Journal {
    override def print(out: PrintStream): Unit = {
      out.print(msg)
    }
  }

  case class CellSpawn(prototypeCell: ID, spawnAt: NodeID) extends Journal {
    override def print(out: PrintStream): Unit = {
      out.print(s"SPAWN $prototypeCell at $spawnAt")
    }
  }

}


class JournalPrinter(items: Iterable[JournalPayload]) {
  def printTo(out: java.io.PrintStream) = {

  }
}

object JournalPrinter {
  def apply(items: Iterable[JournalPayload]) = new JournalPrinter(items)
}

