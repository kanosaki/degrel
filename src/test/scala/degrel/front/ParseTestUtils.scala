package degrel.front

object ParseTestUtils {
  def mkVertex(lbl: String, edges: Iterable[(String, AstFunctor)] = Map(), attributes: Map[String, String] = Map()) = {
    val attrs = if (attributes.isEmpty) {
      None
    } else {
      Some(attributes.map { case (k, v) => AstAttribute(k, v)}.toSeq)
    }
    AstFunctor(AstName(Some(AstLabel(lbl)), None), attrs, mkEdges(edges))
  }

  def mkEdges(edges: Iterable[(String, AstFunctor)]): Seq[AstEdge] = {
    edges.map {
      case (l, v) => AstEdge(AstLabel(l), v)
    }.toSeq
  }

  def mkName(label: String = null, capture: String = null) = {
    AstName(wrapIfNotNull(label, AstLabel), wrapIfNotNull(capture, AstVertexBinding))
  }

  def wrapIfNotNull[X, Y](v: X, wrapper: X => Y): Option[Y] = {
    if (v == null)
      None
    else
      Some(wrapper(v))
  }
}

