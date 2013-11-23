package degrel.core

case class Edge(label: Label, dst: Vertex) extends Product2[String, Vertex] with Element {
  def _1: String = label.expr
  def _2: Vertex = dst

  override def toString: String = {
    s"${label.expr}: $dst"
  }
}

