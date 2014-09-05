package degrel.core

import degrel.rewriting.BuildingContext

class ReferenceVertexBody(label: Label, attrs: Map[String, String], all_edges: Iterable[Edge], _id: ID)
  extends VertexBody(
    label,
    attrs,
    all_edges,
    _id) {
  private lazy val unreferenceEdges = all_edges.filter(!_.isReference)

  override def build(context: BuildingContext): Vertex = {
    val matchedV = context.matchedVertexExact(this.referenceTarget)
    val matchedEdges = this.referenceTarget.edges().map(context.matchedEdgeExact).toSet
    val builtEdges = matchedV
      .edges()
      .filter(!matchedEdges.contains(_))
      .map(_.duplicate()) ++
      unreferenceEdges
        .map(_.build(context))
    Vertex(matchedV.label.expr, builtEdges, matchedV.attributes)
  }

  def referenceTarget: Vertex = {
    val refEdges = this.edges("_ref")
    refEdges.head.dst
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
}

