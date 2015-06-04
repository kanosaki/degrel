package degrel.misc.benchmark

import java.nio.file.{Files, Path}
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.swing.table.DefaultTableModel

import degrel.utils.text.table.TextTable
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods

import scala.collection.JavaConversions._
import scala.language.implicitConversions

/**
 * ターゲットに含まれるすべてのスクリプトを実行し，その実行時間を計測し
 * レポートにまとめます
 * @param targets 実行するスクリプト，またはそれを含むディレクトリ．`*.dg`の拡張子を持つ通常のファイルが対象になります
 * @param resultJson 結果を書き出すパス．JSONで書き出されます
 * @param quiet これを`true`の時，スクリプトの標準出力を無効にします．
 */
class FilesBenchmark(targets: Seq[Path], resultJson: Option[Path], quiet: Boolean = true) {
  val outputEncoding = "utf-8"
  val out = System.out
  var beginTime: LocalDateTime = null

  def start() = {
    if (this.beginTime != null) {
      throw new RuntimeException("Cannot run twice.")
    }
    _println("DEGREL Benchmark")
    val entries = targets.flatMap(mapEntry)
    _println(s"${entries.size} entries found")
    this.beginTime = LocalDateTime.now()
    val reports = entries.map { entry =>
      _println(s"Running $entry")
      entry.exec()
    }
    val finishTime = LocalDateTime.now()
    _println(s"------------- Finished ----------------")
    _println(s"Time:\t\t$beginTime --> $finishTime")
    _println(s"Elapsed:\t${ChronoUnit.MILLIS.between(beginTime, finishTime).toFloat / 1000}s")
    _println(s"---------------------------------------")
    val tw = mkTable(reports)
    tw.printTable()
    resultJson match {
      case Some(resultPath) => {
        val reportJsonObjs = reports.map(_.toJson)
        val json = mkOutputJson(reportJsonObjs)
        resultPath.toFile.getParentFile.mkdirs()
        Files.write(resultPath, json.getBytes(outputEncoding))
      }
      case _ =>
    }
  }

  def mkTable(reports: Seq[ReportUnit]): TextTable = {
    val rows = reports.map(_.toTableRowForPrint).toArray
    val cols = ReportUnit.csvRows.asInstanceOf[Array[AnyRef]]
    val tm = new DefaultTableModel(rows, cols)
    new TextTable(tm)
  }

  def _println(msg: String) = {
    out.println(msg)
  }

  def mkOutputJson(reportJsonObjs: Seq[JObject]) = {
    val jsonObj = ("version" -> s"degrel-benchmark-${degrel.version}") ~
      ("begin" -> degrel.utils.DateTime.strftime(beginTime)) ~
      ("reports" -> reportJsonObjs)
    JsonMethods.pretty(JsonMethods.render(jsonObj))
  }

  def mapEntry(path: Path): Iterator[FileEntry] = {
    if (Files.isDirectory(path)) {
      Files.list(path).iterator().toIterator.filter(entryFilter).map(new FileEntry(_, quiet))
    } else {
      Seq(new FileEntry(path, quiet)).iterator
    }
  }

  def entryFilter(path: Path): Boolean = {
    Files.isRegularFile(path) && path.getFileName.toString.endsWith(".dg")
  }
}
