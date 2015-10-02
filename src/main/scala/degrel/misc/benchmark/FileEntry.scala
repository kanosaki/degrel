package degrel.misc.benchmark

import java.nio.file.Path

import degrel.control.Bootstrapper

class FileEntry(bootstrapper: Bootstrapper, path: Path, quiet: Boolean) {
  def exec(): ReportUnit = {
    val chassis = bootstrapper.createChassis(path.toFile)
    val bi = new BenchmarkInterpreter(chassis, quiet)
    bi.start()
    new ReportUnit(path.toString,
                   bi.totalSteps,
                   bi.startTime,
                   bi.finishTime,
                   bi.initialSize,
                   chassis.diagnostics.spans,
                   bi.rewriteeSetName)
  }

  override def toString: String = {
    this.path.toString
  }
}
