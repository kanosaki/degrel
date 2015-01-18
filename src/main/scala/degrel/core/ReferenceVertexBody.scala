package degrel.core

import degrel.engine.rewriting.BuildingContext

class ReferenceVertexBody(label: Label, attrs: Map[Label, String], all_edges: Iterable[Edge], _id: ID)
  extends VertexBody(
    label,
    attrs,
    all_edges,
    _id) {
  private val unreferenceEdges = all_edges.filter(!_.isReference).toSeq

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
      matchedV
    } else {
      val matchedEdges = this.referenceTarget
        .edges()
        .map(context.matchedEdgeExact)
        .toSet
      val unmatchedEdges = matchedV
        .edges()
        .filter(!matchedEdges.contains(_))
        .map(_.duplicate())
      val margingEdges = unreferenceEdges.map(_.build(context))
      val builtEdges = unmatchedEdges ++ margingEdges
      Vertex(matchedV.label.expr, builtEdges.toSeq, matchedV.attributes)
    }
  }

  override def reprRecursive(history: Trajectory): String = {
    history.walk(this) {
      case Unvisited(nextHistory) => {
        if (all_edges.isEmpty) {
          s"@<${this.referenceTarget.reprRecursive(nextHistory)}>"
        } else {
          val edgesExpr = unreferenceEdges.map(_.reprRecursive(nextHistory)).mkString(", ")
          s"@<${this.referenceTarget.reprRecursive(nextHistory)}($edgesExpr)>"
        }
      }
      case Visited(_) => {
        this.repr
      }
    }
  }

  override def repr: String = {
    s"@<${this.referenceTarget.repr}>"
  }

  def referenceTarget: Vertex = {
    val refEdges = this.edges(SpecialLabels.E_REFERENCE_TARGET)
    refEdges.head.dst
  }
}

