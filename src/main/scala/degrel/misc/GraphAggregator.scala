package degrel.misc

import degrel.core.{Graph, ID, Traverser, Vertex}

import scala.collection.mutable

/**
 * グラフを辿ってテーブルを作成します
 */
class GraphAggregator(val root: Vertex, rootDepth: Option[Int] = None) {
  private[this] val mapping = new mutable.HashMap[ID, Vertex]()

  this.add(root, rootDepth)

  def add(root: Vertex, depth: Option[Int] = None) = {
    val maxHops = depth match {
      case Some(x) => x
      case None => Traverser.UNLIMITED
    }
    for (v <- Traverser(root, maxHops)) {
      mapping.get(v.id) match {
        case Some(prev) =>
          if (prev ne v)
            throw new IllegalStateException(s"Duplicated ID $v")
        case None =>
          mapping += v.id -> v
      }
    }
  }

  def vertices: Iterable[(ID, Vertex)] = mapping

  def toGraph: Graph = {
    ???
  }
}
