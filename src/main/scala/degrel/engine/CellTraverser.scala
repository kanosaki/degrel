package degrel.engine

import degrel.core.Vertex
import degrel.engine.rewriting.RewritingTarget

import scala.collection.mutable

/**
 * Optimized class for Traverser
 * この頂点から「Cellの範囲として適切な」頂点の集合を`RewritingTarget`として返します
 */
class CellTraverser(roots: Iterable[Vertex], self: Driver) extends Iterable[RewritingTarget] {

  class It extends Iterator[RewritingTarget] {
    val remainRoots = mutable.Queue[Vertex]()
    val nextItems = mutable.Queue[Vertex]()
    val visited = mutable.HashSet[Vertex]()
    var currentRoot: Vertex = null

    // init
    remainRoots ++= roots.filter(v => !v.isRule)
    if (remainRoots.nonEmpty) {
      pullItemFromRoots()
    }


    override def hasNext: Boolean = remainRoots.nonEmpty || nextItems.nonEmpty


    private def pullItemFromRoots() = {
      currentRoot = remainRoots.dequeue()
      nextItems.enqueue(currentRoot)
    }

    override def next(): RewritingTarget = {
      if (nextItems.isEmpty) {
        pullItemFromRoots()
      }
      val next = nextItems.dequeue()
      next.edges.foreach { e =>
        val dst = e.dst
        if (!dst.isCell && !visited.contains(dst)) {
          nextItems.enqueue(dst)
          visited += dst
        }
      }
      RewritingTarget(next.asHeader, currentRoot.asHeader, self)
    }
  }

  override def iterator: Iterator[RewritingTarget] = new It()
}

object CellTraverser {
  def apply(root: Vertex, self: Driver): CellTraverser = {
    if (root.isCell) {
      new CellTraverser(root.toCell.roots, self)
    } else {
      new CellTraverser(Seq(root), self)
    }
  }
}
