package degrel.dgspec

import java.io.File
import java.nio.file.Path

class FileDgspec(path: Path) extends Dgspec {
  val spec = {
    val mapper = SpecFile.defaultMapper
    val tree = mapper.readTree(new File(path.toString))
    implicit val factory = new SpecFactory()
    SpecFile.decode(tree)
  }


  override def isIgnored: Boolean = spec.ignored

  override def description: String = {
    s"""$path "${spec.caption}""""
  }

  override def apply() = {
    val context = SpecContext.default()
    spec.evaluate(context)
  }
}
