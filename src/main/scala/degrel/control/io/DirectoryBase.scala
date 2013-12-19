package degrel.control.io

import java.io.File
import degrel.control.env.OS
import scala.io.Source

trait DirectoryBase {
  def file: File

  def init() = {
    if (!file.exists) {
      file.mkdirs()
    }
  }

  def mkPath(relpath: String) = {
    OS.current.pathJoin(this.file.toString, relpath)
  }

  def mkFile(relpath: String, createIfNotExists: Boolean = true) = {
    val ret = new File(this.mkPath(relpath))
    if(!ret.exists && createIfNotExists) {
      ret.createNewFile()
    }
    ret
  }

  def open(relpath: String) = {
    Source.fromFile(this.mkPath(relpath))
  }
}
