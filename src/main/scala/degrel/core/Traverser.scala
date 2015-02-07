package degrel.core

import scala.collection.mutable

/**
 * グラフを幅優先で探索する
 * @param start 起点となる頂点
 * @param maxHops 最大ホップ数
 * @param edgePred 頂点をトラバースする際に，この条件を満たさないエッジはトラバースされません
 * @todo マルチスレッド化?
 */
class Traverser(val start: Vertex,
                val maxHops: Option[Int],
                val edgePred: Edge => Boolean) extends Iterable[Vertex] {
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

    def hasNext: Boolean = vQueue.nonEmpty

    def next(): Vertex = {
      val nextV = vQueue.dequeue()
      vQueue ++= nextV.
        edges().
        filter(e => !vHistory.contains(e.dst) && edgePred(e)).
        map(_.dst)
      vHistory += nextV
      nextV
    }
  }

  class HopLimitedUniqueVertexTraverser(val hopLimit: Int) extends Iterator[Vertex] {
    protected val vQueue = new mutable.Queue[(Vertex, Int)]()
    protected val vHistory = new mutable.HashSet[Vertex]()
    vQueue += start -> 0

    def hasNext: Boolean = vQueue.nonEmpty

    def next(): Vertex = {
      val (nextV, depth) = vQueue.dequeue()
      val nextDepth = depth + 1
      if (nextDepth <= hopLimit) {
        val nextEntries = nextV
          .edges()
          .filter(e => !vHistory.contains(e.dst) && edgePred(e))
          .map(_.dst -> nextDepth)
        vQueue ++= nextEntries
      }
      vHistory += nextV
      nextV
    }
  }

}

object Traverser {
  val UNLIMITED = -1

  val fTrue : Edge => Boolean = _ => true

  def apply(start: Vertex, maxHops: Int = UNLIMITED, edgePred: Edge => Boolean = null) = {
    val ep = edgePred match {
      case null => fTrue
      case other => other
    }
    if (maxHops < 0) {
      new Traverser(start, None, ep)
    } else {
      new Traverser(start, Some(maxHops), ep)
    }
  }
}
