package degrel.control.env

import java.io.File

import degrel.control.io.{AppDirectory, MacAppDirectory}

trait OS {

  def getenv(varname: String, default: String = null) = {
    System.getenv(varname) match {
      case null => default
      case other => other
    }
  }

  def appdir: AppDirectory

  def homedir = System.getProperty("user.home")

  def pathJoin(str: String*) = {
    val sep = File.separator
    str.head.stripSuffix(sep) + sep + str.tail.map(_.stripPrefix(sep).stripSuffix(sep)).mkString(sep)
  }
}

object OS {
  lazy val current = this.mkCurrentOS()

  private def mkCurrentOS(): OS = {
    val os = System.getProperty("os.name")
    if (os.startsWith("Mac")) {
      return new MacOSX()
    }
    throw new RuntimeException(s"Unsupported OS $os")
  }
}

trait UnixLike extends OS {

}

class MacOSX extends UnixLike {
  def appdir: AppDirectory = new MacAppDirectory(this.pathJoin(this.homedir, "Library", "Application Support", "degrel"))
}

