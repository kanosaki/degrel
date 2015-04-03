package degrel.engine.rewriting.molding

import degrel.core._

class ReferenceVertexMolder(val mold: Vertex, val context: MoldingContext) extends Molder {
  private[this] val unreferenceEdges = mold.edges.filter(!_.isReference).toSeq

  override val header: VertexHeader = new LocalVertexHeader(null)

  override def process(ph: MoldPhase): Unit = {
    ph match {
      case MoldPhase.Scan => {
      }
      case MoldPhase.Mold => {
        this.doMold()
      }
    }
    super.process(ph)
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
      // 参照
      val matchedEdges = this.referenceTarget
        .edges
        .map(context.matchedEdgeExact)
        .toSet
      // 新規に構成
      val unmatchedEdges = matchedV
        .edges
        .filter(!matchedEdges.contains(_))
        .map(_.shallowCopy())
      val mergingEdges = moldEdges(unreferenceEdges)
      val builtEdges = unmatchedEdges ++ mergingEdges
      val vb = VertexBody(matchedV.label, matchedV.attributes, builtEdges.toSeq, ID.autoAssign)
      this.header.write(vb)
    }
  }

  def referenceTarget: Vertex = {
    mold.thruSingle(Label.E.ref)
  }
}
