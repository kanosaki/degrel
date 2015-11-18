package degrel.misc.benchmark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import degrel.engine.ProcedureSpan
import org.apache.commons.io.FilenameUtils
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._


class ReportUnit(name: String,
                 totalSteps: Long,
                 begin: LocalDateTime,
                 end: LocalDateTime,
                 initialMainSize: Long,
                 spans: Seq[ProcedureSpan],
                 rewriteeSetName: String = "none") {
  val elapsed = ChronoUnit.MILLIS.between(begin, end)
  val rps = totalSteps.toFloat / (elapsed.toFloat / 1000)
  val datetimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")

  val fpCheckSpan = spans.find(_.name == "fingerprintCheck").get
  val rewriteSpan = spans.find(_.name == "rewrite").get
  val matchSpan = spans.find(_.name == "match").get
  val buildSpan = spans.find(_.name == "build").get

  private def nanoSecToMsec(nanoSec: Long): Long = {
    nanoSec  / Math.pow(1000, 2).toLong
  }

  def toTableRowForPrint: Array[AnyRef] = {
    Array(FilenameUtils.getName(name),
          Long.box(totalSteps),
          rewriteeSetName,
          Long.box(initialMainSize),
          Long.box(elapsed),
          Float.box(rps),
          Long.box(fpCheckSpan.callCount),
          Long.box(rewriteSpan.callCount),
          Long.box(nanoSecToMsec(rewriteSpan.accNanoTime)),
          Long.box(nanoSecToMsec(fpCheckSpan.accNanoTime)),
          Long.box(nanoSecToMsec(matchSpan.accNanoTime)),
          Long.box(nanoSecToMsec(buildSpan.accNanoTime))
    )
  }

  def toJson: JObject = {
    ("name" -> name) ~
      ("totalSteps" -> totalSteps) ~
      ("begin" -> degrel.utils.DateTime.strftime(begin)) ~
      ("end" -> degrel.utils.DateTime.strftime(end)) ~
      ("elapsed" -> elapsed) ~
      ("rps" -> rps) ~
      ("spans" -> spans.map(ps => (ps.name, ps.toJson)).toMap) ~
      ("initialMainSize" -> initialMainSize)
  }
}

object ReportUnit {
  val csvRows = Array(
    "Name", "Steps", "Rewritee",
    "Size", "Elapsed", "RPS", "FpTry", "RwTry",
    "Rewrite", "Fp", "Match", "Build")
}
