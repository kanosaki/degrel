package degrel.dgspec

import java.io.File
import java.nio.file.Path
import java.util.{Map => JMap}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

class DgspecFile(path: Path) extends Dgspec {
  val node = {
    val mapper = new ObjectMapper(new YAMLFactory())
    mapper.readTree(new File(path.toString))
  }

  override def description: String = {
    s"""$path "$specDescription""""
  }

  def specDescription: String = node.get("description").asText()

  override def apply() = {

  }
}
