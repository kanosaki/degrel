package degrel.cluster.journal

import scala.collection.mutable
import scala.reflect.runtime.universe

trait JournalFilter {
  def byType(journalType: universe.Type): Boolean

  def byValue(journalValue: Journal): Boolean
}

object JournalFilter {
  val default = new ThroughJournalFilter()
}

class ThroughJournalFilter extends JournalFilter {
  override def byType(journalType: universe.Type): Boolean = true

  override def byValue(journalValue: Journal): Boolean = true
}

class WhiteListJournalFilter extends JournalFilter {
  private val acceptableTypeSet: mutable.Set[String] = mutable.HashSet()

  def accept[T <: Journal : universe.TypeTag]: this.type = {
    acceptableTypeSet += universe.typeOf[T].toString
    this
  }

  override def byType(journalType: universe.Type): Boolean = {
    acceptableTypeSet.contains(journalType.toString)
  }

  override def byValue(journalValue: Journal): Boolean = true
}
