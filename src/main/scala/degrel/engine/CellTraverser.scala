package degrel.engine

import degrel.core.{Cell, Vertex}

import scala.collection.mutable

/**
 * Optimized class for Traverser
 */
class CellTraverser(target: Cell) extends Iterable[Vertex] {

  class It extends Iterator[Vertex] {
    val nextItems = mutable.Queue[Vertex]()
    val visited = mutable.HashSet[Vertex]()

    nextItems ++= target.roots

    override def hasNext: Boolean = nextItems.nonEmpty

    override def next(): Vertex = {
      val next = nextItems.dequeue()
      next.edges.foreach { e =>
        val dst = e.dst
        if (!dst.isCell && !visited.contains(dst)) {
          nextItems.enqueue(dst)
          visited += dst
        }
      }
      next
    }
  }

  override def iterator: Iterator[Vertex] = new It()
}

object CellTraverser {
  def apply(cell: Cell): CellTraverser = {
    new CellTraverser(cell)
  }
}
