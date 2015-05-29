package degrel.misc.benchmark

import java.nio.file.Path

class FileEntry(path: Path, quiet: Boolean) {
  def exec(): ReportUnit = {
    val bi = new BenchmarkInterpreter(path.toFile, quiet)
    bi.start()
    new ReportUnit(path.toString,
                   bi.totalSteps,
                   bi.startTime,
                   bi.finishTime,
                   bi.initialSize)
  }

  override def toString: String = {
    this.path.toString
  }
}
