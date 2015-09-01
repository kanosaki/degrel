package degrel.misc.benchmark

import java.nio.file.Path

import degrel.control.Bootstrapper

class FileEntry(bootstrapper: Bootstrapper, path: Path, quiet: Boolean) {
  def exec(): ReportUnit = {
    val chassis = bootstrapper.createChassis(path.toFile)
    val bi = new BenchmarkInterpreter(chassis, quiet)
    bi.start()
    val diag = bi.chassis.diagnostics
    new ReportUnit(path.toString,
                   bi.totalSteps,
                   bi.startTime,
                   bi.finishTime,
                   bi.initialSize,
                   diag.rewriteTryCount,
                   diag.rewriteSpan.callCount,
                   diag.rewriteSpan.accNanoTime,
                   diag.matchSpan.accNanoTime,
                   diag.buildSpan.accNanoTime,
                   bi.rewriteeSetName)
  }

  override def toString: String = {
    this.path.toString
  }
}
