package degrel.core

import degrel.rewriting.BuildingContext

class ReferenceVertexBody(label: Label, attrs: Map[String, String], all_edges: Iterable[Edge], _id: ID)
  extends VertexBody(
    label,
    attrs,
    all_edges,
    _id) {
  private lazy val unreferenceEdges = all_edges.filter(!_.isReference)

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
      val matchedEdges = this.referenceTarget.edges().map(context.matchedEdgeExact).toSet
      val builtEdges = matchedV
        .edges()
        .filter(!matchedEdges.contains(_))
        .map(_.duplicate()) ++
        unreferenceEdges
          .map(_.build(context))
      Vertex(matchedV.label.expr, builtEdges, matchedV.attributes)
    }
  }

  override def reprRecursive(history: Trajectory): String = {
    history.walk(this) {
      case Right(nextHistory) => {
        if (all_edges.isEmpty) {
          s"@<${this.referenceTarget.reprRecursive(nextHistory)}>"
        } else {
          val edgesExpr = unreferenceEdges.map(_.reprRecursive(nextHistory)).mkString(", ")
          s"@<${this.referenceTarget.reprRecursive(nextHistory)}($edgesExpr)>"
        }
      }
      case Left(_) => {
        this.repr
      }
    }
  }

  override def repr: String = {
    s"@<${this.referenceTarget.repr}>"
  }

  def referenceTarget: Vertex = {
    val refEdges = this.edges("_ref")
    refEdges.head.dst
  }
}

