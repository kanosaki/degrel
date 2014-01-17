package degrel.core

import degrel.rewriting.BuildingContext

class ReferenceVertexBody(label: Label, attrs: Map[String, String], all_edges: Iterable[Edge]) extends VertexBody(label,
                                                                                                                   attrs,
                                                                                                                   all_edges) {
  private lazy val unreferenceEdges = all_edges.filter(!_.isReference)

  override def repr: String = {
    s"@<${this.referenceTarget.repr}>"
  }

  override def build(context: BuildingContext): Vertex = {
    val matchedV = context.matchOf(this.referenceTarget)
    val matchedEdges = this.referenceTarget.edges().map(context.matchedEdge).toSet
    val builtEdges = matchedV.edges().filter(!matchedEdges.contains(_)) ++ unreferenceEdges.map(_.build(context))
    Vertex(matchedV.label.expr, builtEdges, matchedV.attributes)
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

  def referenceTarget: Vertex = {
    val refEdges = this.edges("_ref")
    refEdges.head.dst
  }
}

