package degrel.control

import java.io.File

import degrel.engine.Chassis
import org.apache.commons.io.FileUtils

class ScriptLoader {

  def loadMain(mainFile: File): Chassis = {
    val src = FileUtils.readFileToString(mainFile)
    val main = degrel.parseCell(src)
    Chassis.create(main)
  }
}

object ScriptLoader {
  def apply() = {
    new ScriptLoader()
  }
}
