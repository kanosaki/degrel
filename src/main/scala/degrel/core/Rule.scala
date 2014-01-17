package degrel.core

trait Rule extends Vertex {
  def rhs: Vertex

  def lhs: Vertex

  override def reprRecursive(history: Trajectory) = {
    history.walk(this) {
      case Right(nextHistory) => {
        s"${lhs.reprRecursive(nextHistory)} -> ${rhs.reprRecursive(nextHistory)}"
      }
      case Left(_) => throw new RuntimeException("Cannot repr a Rule recursively")
    }
  }
}


object Rule {
  def apply(lhs: Vertex, rhs: Vertex) = {
    new RuleVertexHeader(lhs, rhs)
  }
}
