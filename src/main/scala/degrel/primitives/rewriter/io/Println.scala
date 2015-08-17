package degrel.primitives.rewriter.io

import degrel.core._
import degrel.engine.rewriting.{RewritingTarget, RewriteResult, Rewriter, RewritingTarget$}
import degrel.utils.PrettyPrintOptions

class Println extends Rewriter {
  val printlnLabel = Label("println")

  override def isPartial: Boolean = false

  override def rewrite(rt: RewritingTarget): RewriteResult = {
    val target = rt.target
    val self = rt.self
    if (target.label != printlnLabel) return nop
    val numberedEdges = target.edges.filter(_.label.expr.forall(_.isDigit)).toSeq.sortBy(_.label)
    val printNeighbors = numberedEdges.map(_.dst)
    val text = printNeighbors.map(mapString).mkString(" ")
    // Output to driver stdout
    self.resource.console.stdout.println(text)

    write(rt, Cell())
  }

  def mapString(v: Vertex): String = v.getValue[String] match {
    case Some(s) => s
    case None => v.pp
  }

  override def pp(implicit opt: PrettyPrintOptions): String = "<Built-in println>"

  override def pattern: Vertex = parse("println")
}
