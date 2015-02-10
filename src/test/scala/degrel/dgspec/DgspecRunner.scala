package degrel.dgspec

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import org.scalatest.FlatSpec

import scala.collection.mutable

class DgspecRunner extends FlatSpec {
  val specDirs = Seq("dgspec")
    .map(Paths.get(_))
    .filter(Files.isDirectory(_))

  val specs = specDirs.flatMap(collectSpecs)

  specs.foreach(s => {
    if (!s.isIgnored) {
      this.registerTest(s.description, s.specTags: _*)(s)
    } else {
      this.registerIgnoredTest(s.description, s.specTags: _*)(s)
    }
  })

  def collectSpecs(dir: Path): Seq[Dgspec] = {
    val visitor = new SpecFinder()
    Files.walkFileTree(dir, visitor)
    visitor.foundSpecs.toSeq
  }

  class SpecFinder extends SimpleFileVisitor[Path] {
    val foundSpecs: mutable.ListBuffer[Dgspec] = mutable.ListBuffer()

    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (!attrs.isRegularFile) return FileVisitResult.CONTINUE
      val filename = file.getFileName.toString.toLowerCase
      if (filename.endsWith(".dgspec")
        && filename.endsWith(".yaml")
        && filename.endsWith(".yml")) {
        foundSpecs += new DgspecFile(file)
      }
      FileVisitResult.CONTINUE
    }
  }

}
