package degrel.core

import degrel.engine.rewriting.molding.MoldingContext


object VertexBody {
  def apply(label: Label, attributes: Map[Label, String], allEdges: Iterable[Edge], id: ID): VertexBody = {
    label match {
      case Label.V.reference => new ReferenceVertexBody(label, attributes, allEdges)
      case _ => new LocalVertexBody(label, attributes, allEdges)
    }
  }
}

trait VertexBody extends Vertex {
  def attr(key: Label): Option[String] = {
    attributes.get(key)
  }

  def reprLabel: String = {
    this.attributes.get("__captured_as__") match {
      case Some(capExpr) => s"$capExpr[${this.label.expr}]"
      case None => s"${this.label.expr}"
    }
  }

  def reprAttrs: String = {
    val targetKvs = attributes
      .filter { case (k, v) => !k.isMeta}
    if (targetKvs.nonEmpty) {
      val kvsExpr = targetKvs.map { case (k, v) => s"$k:$v"}.mkString(", ")
      s"{$kvsExpr}"
    } else {
      ""
    }
  }

  override def id: ID = new LocalID(-1)
}

