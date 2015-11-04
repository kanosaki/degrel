package degrel.engine.rewriting.molding

import degrel.core._

/**
 * 参照頂点についてMoldを実行します
 * @param mold 使用する`Mold Vertex`
 * @param context この`Molder`における`MoldingContext`
 */
class ReferenceVertexMolder(val mold: Vertex, val context: MoldingContext) extends Molder {
  /**
   * Pattern vertex.
   */
  val referenceTarget: Vertex = {
    this.mold.thru(Label.E.ref).toList match {
      case v :: Nil => v
      case _ => throw new RuntimeException("Malformed vertex.")
    }
  }

  /**
   * Mold edges.
   */
  val unreferenceEdges: Seq[Edge] = {
    this.mold.edges.filter(e => !e.isReference && !e.isOthers).toSeq
  }

  val matchedVertex = this.context.matchedVertexExact(this.referenceTarget)

  override val header: VertexHeader = new LocalVertexHeader(null)

  override def onPhase(ph: MoldPhase): Unit = ph match {
    case MoldPhase.Mold => {
      this.doMold()
    }
    case _ =>
  }

  /**
   * 束縛に従って頂点を生成・参照します
   * "Bind"のように接続部を持たない場合はパターンマッチした頂点への参照を保持します
   * "Bind(foo: bar)"のように接続部を持つ場合は，Matched頂点, Pattern頂点, Building頂点すべての接続がマージされます
   * 例えば，{@code A(b: b) -> x(y: A(c: c))}が{@code foo(a: a, b: b)}にマッチしたとき，
   * {@code x(y: foo(a: a, b: b, c: c)}が構成されます
   */
  private def doMold(): Unit = {
    val matchedV = context.matchedVertexExact(referenceTarget)
    if (unreferenceEdges.isEmpty) {
      // マッチした頂点への参照を保ってbuildします
      val h = matchedV.asInstanceOf[VertexHeader]
      this.header.write(h.body)
    } else {
      val othersEs = this.importingEdges.toSet
      // 参照
      val matchedEdges = this.referenceTarget
        .edges
        .map(context.matchedEdgeExact)
        .toSet
      // 新規に構成
      val unmatchedEdges = matchedV
        .edges
        .filter(e => !matchedEdges.contains(e) && !othersEs.contains(e))
        .map(_.shallowCopy())
      val mergingEdges = moldEdges(unreferenceEdges)
      val builtEdges = unmatchedEdges ++ mergingEdges ++ othersEs
      val vb = VertexBody(matchedV.label, matchedV.attributes, builtEdges.toSeq, ID.NA)
      this.header.write(vb)
    }
  }

}
