package degrel.front

object ParseTestUtils {
  def mkVertex(lbl: String, edges: Iterable[(String, AstVertex)] = Map(), attributes: Map[String, String] = Map()) = {
    val attrs = if (attributes.isEmpty) {
      None
    } else {
      Some(attributes.map { case (k, v) => AstAttribute(k, v)}.toSeq)
    }
    AstVertex(AstName(None, Some(AstLabel(lbl))), attrs, mkEdges(edges))
  }

  def mkEdges(edges: Iterable[(String, AstVertex)]): Seq[AstEdge] = {
    edges.map {
      case (l, v) => AstEdge(AstLabel(l), v)
    }.toSeq
  }

  def mkName(label: String = null, capture: String = null) = {
    AstName(wrapIfNotNull(capture, AstVertexBinding), wrapIfNotNull(label, AstLabel))
  }

  def wrapIfNotNull[X, Y](v: X, wrapper: X => Y): Option[Y] = {
    if (v == null)
      None
    else
      Some(wrapper(v))
  }
}

