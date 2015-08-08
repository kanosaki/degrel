package degrel.primitives.rewriter.io

import degrel.core._
import degrel.engine.Driver
import degrel.engine.rewriting.{RewriteResult, Rewriter}
import degrel.utils.PrettyPrintOptions

class Println extends Rewriter {
  val printlnLabel = Label("println")

  override def isPartial: Boolean = false

  override def rewrite(self: Driver, target: VertexHeader): RewriteResult = {
    if (target.label != printlnLabel) return nop
    val numberedEdges = target.edges.filter(_.label.expr.forall(_.isDigit)).toSeq.sortBy(_.label)
    val printNeighbors = numberedEdges.map(_.dst)
    val text = printNeighbors.map(mapString).mkString(" ")
    // Output to driver stdout
    self.resource.console.stdout.println(text)

    write(target, Cell())
  }

  def mapString(v: Vertex): String = v.getValue[String] match {
    case Some(s) => s
    case None => v.pp
  }

  override def pp(implicit opt: PrettyPrintOptions): String = "<Built-in println>"
}
