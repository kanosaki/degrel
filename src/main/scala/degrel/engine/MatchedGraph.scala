package degrel.engine

// Bridges data graph and rule graph


trait MatchedGraph extends Iterable[Binding] {
  def success: Boolean = true
}

trait MatchedVertex extends MatchedGraph {

}

case class SingleMatchedVertex(vBind: VertexBind, eMatches: Iterable[MatchedEdge]) extends MatchedVertex {
  def iterator: Iterator[Binding] = ???
}

case class MultiplexVertexMatch(matches: Iterable[MatchedVertex]) extends MatchedVertex {
  def iterator: Iterator[Binding] = ???
}

trait MatchedEdge extends MatchedGraph {

}

case class SingleMatchedEdge (eBind: EdgeBind, vMatch: MatchedVertex) extends MatchedEdge {
  def iterator: Iterator[Binding] = ???
}

case object NoMatch extends MatchedGraph with MatchedVertex with MatchedEdge {
  def iterator: Iterator[Binding] = Seq().iterator
  override def success: Boolean = false
}
