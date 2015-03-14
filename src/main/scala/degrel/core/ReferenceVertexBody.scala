package degrel.core

import degrel.engine.rewriting.BuildingContext

class ReferenceVertexBody(label: Label, attrs: Map[Label, String], all_edges: Iterable[Edge])
  extends LocalVertexBody(
    label,
    attrs,
    all_edges) {
  private[this] val unreferenceEdges = all_edges.filter(!_.isReference).toSeq

  /**
   * 束縛に従って頂点を生成・参照します
   * "Bind"のように接続部を持たない場合はパターンマッチした頂点への参照を保持します
   * "Bind(foo: bar)"のように接続部を持つ場合は，Matched頂点, Pattern頂点, Building頂点すべての接続がマージされます
   * 例えば，{@code A(b: b) -> x(y: A(c: c))}が{@code foo(a: a, b: b)}にマッチしたとき，
   * {@code x(y: foo(a: a, b: b, c: c)}が構成されます
   */
  override def build(context: BuildingContext): Vertex = {
    val matchedV = context.matchedVertexExact(this.referenceTarget)
    if (unreferenceEdges.isEmpty) {
      // マッチした頂点への参照を保ってbuildします
      val h = matchedV.asInstanceOf[VertexHeader]
      new LocalVertexHeader(h.body)
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
      val margingEdges = unreferenceEdges.map(_.build(context))
      val builtEdges = unmatchedEdges ++ margingEdges
      Vertex(matchedV.label.expr, builtEdges.toSeq, matchedV.attributes)
    }
  }

  def referenceTarget: Vertex = {
    val refEdges = this.edgesWith(SpecialLabels.E_REFERENCE_TARGET)
    refEdges.head.dst
  }
}

