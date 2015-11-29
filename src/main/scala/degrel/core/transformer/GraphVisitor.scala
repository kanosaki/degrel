package degrel.core.transformer

import degrel.core.{Traverser, Vertex}

class GraphVisitor(val modules: Seq[VisitModule], traverser: Vertex => Iterable[Vertex]) {
  def visit(v: Vertex) = {
    traverser(v).foreach(this.callVisitors)
  }

  def callVisitors(v: Vertex) = {
    modules.foreach(_.visit(v, this))
  }
}

object GraphVisitor {
  def apply(traverser: Vertex => Iterable[Vertex], modules: VisitModule*) = {
    new GraphVisitor(modules, traverser)
  }

  def apply(modules: VisitModule*) = {
    new GraphVisitor(modules.toSeq, Traverser.apply)
  }
}

