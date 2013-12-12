package degrel.rewriting

// Bridges data graph and rule graph


trait MatchedGraph /* extends Iterable[Binding] */ {
  def success: Boolean = true

  def pack: BindingPack
}

trait VertexMatching extends MatchedGraph {

}

case class MonoVertexMatching(vBind: VertexBridge, eMatches: Iterable[EdgeMatching]) extends VertexMatching {
  def iterator: Iterator[BindingPack] = ???

  private lazy val _success: Boolean = eMatches.isEmpty || eMatches.forall(_.success)

  override def success = _success

  def pack: BindingPack = {
    eMatches.map(_.pack).fold(MonoBindingPack(Stream(vBind)))(_ ++ _)
  }
}

case class PolyVertexMatching(matches: Iterable[VertexMatching]) extends VertexMatching {
  def iterator: Iterator[BindingPack] = ???

  private lazy val _success: Boolean = !matches.isEmpty

  override def success = _success

  def pack: BindingPack = {
    PolyBindingPack(matches.map(_.pack))
  }
}

trait EdgeMatching extends MatchedGraph {

}

case class MonoEdgeMatching(eBind: EdgeBridge, vMatch: VertexMatching) extends EdgeMatching {
  def iterator: Iterator[BindingPack] = ???

  override def success = vMatch.success

  def pack: BindingPack = {
    MonoBindingPack(Stream(eBind)) ++ vMatch.pack
  }
}

case object NoMatching extends MatchedGraph with VertexMatching with EdgeMatching {
  def iterator: Iterator[BindingPack] = Seq().iterator

  override def success: Boolean = false

  def pack: BindingPack = {
    MonoBindingPack(Stream())
  }
}
