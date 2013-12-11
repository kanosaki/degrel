package degrel.core

import scala.collection.mutable

/**
 * グラフを幅優先で探索する
 * @param start 起点となる頂点
 * @param maxHops 最大ホップ数
 * @todo マルチスレッド化?
 */
class Traverser(val start: Vertex, val maxHops: Option[Int] = None) extends Iterable[Vertex] {
  maxHops match {
    case Some(mh) if mh < 0 => throw new IllegalArgumentException("maxHopsは0以上の数である必要があります")
    case _ =>
  }

  def iterator: Iterator[Vertex] = {
    maxHops match {
      case Some(limit) => new HopLimitedUniqueVertexTraverser(limit)
      case None => new UniqueVertexTraverser()
    }
  }

  class UniqueVertexTraverser extends Iterator[Vertex] {
    protected val vQueue = new mutable.Queue[Vertex]()
    protected val vHistory = new mutable.HashSet[Vertex]()
    vQueue += start
    vHistory += start

    def hasNext: Boolean = !vQueue.isEmpty

    def next(): Vertex = {
      val nextV = vQueue.dequeue()
      vQueue ++= nextV.edges().map(_.dst).filter(!vHistory.contains(_))
      vHistory += nextV
      nextV
    }
  }

  class HopLimitedUniqueVertexTraverser(val hopLimit: Int) extends Iterator[Vertex] {
    protected val vQueue = new mutable.Queue[(Vertex, Int)]()
    protected val vHistory = new mutable.HashSet[Vertex]()
    vQueue += start -> 0

    def hasNext: Boolean = !vQueue.isEmpty

    def next(): Vertex = {
      val (nextV, depth) = vQueue.dequeue()
      val nextDepth = depth + 1
      if (nextDepth <= hopLimit) {
        val nextEntries = nextV.edges()
          .map(_.dst)
          .filter(!vHistory.contains(_))
          .map(_ -> nextDepth)
        vQueue ++= nextEntries
      }
      vHistory += nextV
      nextV
    }
  }
}

object Traverser {
  def apply(start: Vertex, maxHops: Int = -1) = {
    if(maxHops < 0){
      new Traverser(start, None)
    } else {
      new Traverser(start, Some(maxHops))
    }
  }
}
