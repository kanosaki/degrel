package degrel.core.transformer

import degrel.core.Vertex
import degrel.core.transformer.VisitOrder.BreadthFirst

import scala.annotation.tailrec
import scala.collection.mutable

class GraphVisitor(val modules: Seq[VisitModule], val order: VisitOrder) {
  import VisitOrder._

  def visit(root: Vertex) = {
    val visitorImpl = order match {
      case DepthFirst => new DFS(root)
      case BreadthFirst => new BFS(root)
    }
    if (isAcceptable(root)) {
      visitorImpl.process()
    }
  }

  private def isAcceptable(v: Vertex): Boolean = {
    modules.forall(_.isAcceptable(v, this))
  }

  protected abstract class VisitorImpl {
    val visited = mutable.HashSet[Vertex]()

    def callVisitors(v: Vertex) = {
      modules.foreach(_.visit(v, GraphVisitor.this))
    }

    def process(): Unit
  }

  protected class BFS(begin: Vertex) extends VisitorImpl {
    val nextItems = mutable.Queue[Vertex]()
    nextItems.enqueue(begin)

    @tailrec
    final override def process(): Unit = {
      if (nextItems.isEmpty) return
      val next = nextItems.dequeue()
      callVisitors(next)
      next.edges.foreach { e =>
        val dst = e.dst
        if (!visited.contains(dst)) {
          if (isAcceptable(dst)) {
            nextItems.enqueue(dst)
          }
          visited += dst
        }
      }
      process()
    }
  }

  protected class DFS(begin: Vertex) extends VisitorImpl {
    val stack = mutable.Stack[Vertex]()

    override def process() = {
      @tailrec
      def processVertex(v: Vertex): Unit = {
        callVisitors(v)
        v.edges.foreach { e =>
          val dst = e.dst
          if (!visited.contains(dst)) {
            if (isAcceptable(dst)) {
              stack.push(dst)
            }
            visited += dst
          }
        }
        if (stack.nonEmpty) {
          processVertex(stack.pop())
        }
      }
      processVertex(begin)
    }
  }

}

object GraphVisitor {
  def apply(modules: VisitModule*) = {
    new GraphVisitor(modules.toSeq, BreadthFirst)
  }
}

trait VisitOrder

object VisitOrder {
  case object DepthFirst extends VisitOrder

  case object BreadthFirst extends VisitOrder
}


