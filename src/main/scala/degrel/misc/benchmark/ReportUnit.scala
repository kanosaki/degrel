package degrel.misc.benchmark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._


class ReportUnit(name: String,
                 totalSteps: Long,
                 begin: LocalDateTime,
                 end: LocalDateTime,
                 initialMainSize: Long) {
  val elapsed = ChronoUnit.MILLIS.between(begin, end)
  val rps = totalSteps.toFloat / (elapsed.toFloat / 1000)
  val datetimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")

  def toTableRowForPrint: Array[AnyRef] = {
    Array(name,
          Long.box(totalSteps),
          Long.box(initialMainSize),
          Long.box(elapsed),
          Float.box(rps))
  }

  def toJson: JObject = {
    ("name" -> name) ~
      ("totalSteps" -> totalSteps) ~
      ("begin" -> degrel.utils.DateTime.strftime(begin)) ~
      ("end" -> degrel.utils.DateTime.strftime(end)) ~
      ("elapsed" -> elapsed) ~
      ("rps" -> rps) ~
      ("initialMainSize" -> initialMainSize)
  }
}

object ReportUnit {
  val csvRows = Array("Name", "Steps", "Size", "Elapsed", "RPS")
}
