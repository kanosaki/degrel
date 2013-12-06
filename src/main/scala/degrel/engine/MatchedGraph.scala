package degrel.engine

// Bridges data graph and rule graph


trait MatchedGraph /* extends Iterable[Binding] */ {
  def success: Boolean = true
}

trait VertexMatching extends MatchedGraph {

}

case class MonoVertexMatching(vBind: VertexBridge, eMatches: Iterable[EdgeMatching]) extends VertexMatching {
  def iterator: Iterator[Binding] = ???

  private lazy val _success: Boolean = eMatches.isEmpty || eMatches.forall(_.success)

  override def success = _success
}

case class PloyVertexMatching(matches: Iterable[VertexMatching]) extends VertexMatching {
  def iterator: Iterator[Binding] = ???

  private lazy val _success: Boolean = matches.exists(_.success)

  override def success = _success
}

trait EdgeMatching extends MatchedGraph {

}

case class MonoEdgeMatching(eBind: EdgeBridge, vMatch: VertexMatching) extends EdgeMatching {
  def iterator: Iterator[Binding] = ???

  override def success = vMatch.success
}

case object NoMatching extends MatchedGraph with VertexMatching with EdgeMatching {
  def iterator: Iterator[Binding] = Seq().iterator

  override def success: Boolean = false
}
