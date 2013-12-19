package degrel.control.io

import java.io.File

/**
 * アプリケーション用のデータを格納するフォルダ
 */
trait AppDirectory extends DirectoryBase {
  lazy val history = this.mkFile("console_history")
}

object AppDirectory {
  def apply(path: String) = {
    ???
  }
}

class MacAppDirectory(path: String) extends AppDirectory {
  val file = new File(path)
  super.init()
}
