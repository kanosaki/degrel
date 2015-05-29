package degrel.core

class BloomHash(val value: Int) extends  AnyVal {

}

class VertexHash(val value: Seq[Byte]) extends AnyVal {

  def toBinExpr: String = {
    ???
  }
}

object VertexHash {
  def apply(v: Vertex): VertexHash = {
    ???
  }
}
