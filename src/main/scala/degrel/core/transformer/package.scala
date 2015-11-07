package degrel.core

package object transformer {
  def own(target: Vertex, owner: Vertex) = {
    val visitor = GraphVisitor(new AcquireOwnerVisitor(owner))
    visitor.visit(target)
  }
}
