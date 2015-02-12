package degrel.dgspec

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import org.scalatest.FlatSpec

import scala.collection.mutable

/**
 * `dgspec`ファイルを探索し，ユニットテストとして登録します
 */
class DgspecRunner extends FlatSpec {
  val specDirs = Seq("dgspec")
    .map(Paths.get(_))
    .filter(Files.isDirectory(_))

  val specs = specDirs.flatMap(collectSpecs)

  specs.foreach(spec => {
    if (!spec.isIgnored) {
      this.registerTest(spec.description, spec.specTags: _*) {
        spec()
      }
    } else {
      this.registerIgnoredTest(spec.description, spec.specTags: _*) {
        spec()
      }
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
        foundSpecs += new FileDgspec(file)
      }
      FileVisitResult.CONTINUE
    }
  }

}
