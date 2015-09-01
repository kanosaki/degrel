package degrel.misc.benchmark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import org.apache.commons.io.FilenameUtils
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._


class ReportUnit(name: String,
                 totalSteps: Long,
                 begin: LocalDateTime,
                 end: LocalDateTime,
                 initialMainSize: Long,
                 rewriteTryCount: Long,
                 rewriteExecCount: Long,
                 rewriteNanoTime: Long,
                 matchNanoTime: Long,
                 builtNanoTime: Long,
                 rewriteeSetName: String) {
  val elapsed = ChronoUnit.MILLIS.between(begin, end)
  val rps = totalSteps.toFloat / (elapsed.toFloat / 1000)
  val datetimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")

  private def long2DoubleSec(nanoSec: Long): Double = {
    nanoSec.toDouble / Math.pow(1000, 3)
  }

  def toTableRowForPrint: Array[AnyRef] = {
    Array(FilenameUtils.getName(name),
          Long.box(totalSteps),
          rewriteeSetName,
          Long.box(initialMainSize),
          Long.box(elapsed),
          Float.box(rps),
          Long.box(rewriteTryCount),
          Double.box(long2DoubleSec(rewriteNanoTime)),
          Double.box(long2DoubleSec(matchNanoTime)),
          Double.box(long2DoubleSec(builtNanoTime)),
          Long.box(rewriteExecCount))
  }

  def toJson: JObject = {
    ("name" -> name) ~
      ("totalSteps" -> totalSteps) ~
      ("begin" -> degrel.utils.DateTime.strftime(begin)) ~
      ("end" -> degrel.utils.DateTime.strftime(end)) ~
      ("elapsed" -> elapsed) ~
      ("rps" -> rps) ~
      ("rewriteTryCount" -> rewriteTryCount) ~
      ("rewriteExecCount" -> rewriteExecCount) ~
      ("rewriteeSetName" -> rewriteeSetName) ~
      ("rewriteNanoTime" -> rewriteNanoTime) ~
      ("matchNanoTime" -> matchNanoTime) ~
      ("builtNanoTime" -> builtNanoTime) ~
      ("initialMainSize" -> initialMainSize)
  }
}

object ReportUnit {
  val csvRows = Array(
    "Name", "Steps", "Rewritee",
    "Size", "Elapsed", "RPS", "Try",
    "Rewrite", "Match", "Build", "Exec")
}
