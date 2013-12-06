package degrel.engine

// Bridges data graph and rule graph


trait MatchedGraph /* extends Iterable[Binding] */ {
  def success: Boolean = true
}

trait MatchedVertex extends MatchedGraph {

}

case class MonoVertexMatching(vBind: VertexBind, eMatches: Iterable[MatchedEdge]) extends MatchedVertex {
  def iterator: Iterator[Binding] = ???

  private lazy val _success: Boolean = eMatches.isEmpty || eMatches.forall(_.success)

  override def success = _success
}

case class PloyVertexMatching(matches: Iterable[MatchedVertex]) extends MatchedVertex {
  def iterator: Iterator[Binding] = ???

  private lazy val _success: Boolean = matches.exists(_.success)

  override def success = _success
}

trait MatchedEdge extends MatchedGraph {

}

case class MonoEdgeMatching(eBind: EdgeBind, vMatch: MatchedVertex) extends MatchedEdge {
  def iterator: Iterator[Binding] = ???

  override def success = vMatch.success
}

case object NoMatching extends MatchedGraph with MatchedVertex with MatchedEdge {
  def iterator: Iterator[Binding] = Seq().iterator

  override def success: Boolean = false
}
