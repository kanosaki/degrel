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
                val cutOff: TraverserCutOff) extends Iterable[Vertex] {
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
    protected val outputQueue = new mutable.Queue[Vertex]()
    protected val wallQueue = new mutable.Queue[Vertex]()
    protected val innerQueue = new mutable.Queue[Vertex]()
    protected val vHistory = new mutable.HashSet[Vertex]()

    if (cutOff.isWall(start)) {
      wallQueue += start
    } else {
      innerQueue += start
    }
    this.queueNext()

    def queueNext(): Unit = {
      if (!this.queueInner()) {
        this.queueWall()
      }
    }

    def queueInner(): Boolean = {
      var found = false
      while (!found && innerQueue.nonEmpty) {
        val checkV = innerQueue.dequeue()
        vHistory += checkV
        if (cutOff.region.inner) {
          outputQueue += checkV
          found = true
        }
        checkV.neighbors.foreach { v =>
          if (!vHistory.contains(v)) {
            val isWall = cutOff.isWall(v)
            if (isWall) {
              wallQueue += v
            } else {
              innerQueue += v
            }
          }
        }
      }
      found
    }

    def queueWall(): Boolean = {
      var found = false
      while (!found && wallQueue.nonEmpty) {
        val checkV = wallQueue.dequeue()
        vHistory += checkV
        if (cutOff.region.wall) {
          outputQueue += checkV
          found = true
        }
        checkV.neighbors.foreach { v =>
          if (!vHistory.contains(v)) {
            val isWall = cutOff.isWall(v)
            if (isWall) {
              wallQueue += v
            }
          }
        }
      }
      found
    }

    def hasNext: Boolean = outputQueue.nonEmpty

    def next(): Vertex = {
      val ret = outputQueue.dequeue()
      if (outputQueue.isEmpty) {
        this.queueNext()
      }
      ret
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
          .edges
          .filter(e => !vHistory.contains(e.dst))
          .map(_.dst -> nextDepth)
        vQueue ++= nextEntries
      }
      vHistory += nextV
      nextV
    }
  }

}

case class TraverserCutOff(isWallFn: Vertex => Boolean, region: TraverseRegion, thruSet: Set[Vertex] = Set()) {
  def isWall(v: Vertex): Boolean = {
    isWallFn(v) && !thruSet.contains(v)
  }
}

object TraverserCutOff {
  def default = TraverserCutOff(_ => false, TraverseRegion.AllArea)

  def cell(root: Vertex) = TraverserCutOff(_.label == Label.V.cell, TraverseRegion.InnerOnly, Set(root))
}

object Traverser {
  val UNLIMITED = -1

  def apply(start: Vertex): Traverser = {
    Traverser(start, UNLIMITED)
  }

  def apply(start: Vertex,
            maxHops: Int): Traverser = {
    Traverser(start, TraverserCutOff.default, maxHops)
  }

  def apply(start: Vertex, cutoff: TraverserCutOff): Traverser = {
    Traverser(start, cutoff, UNLIMITED)
  }

  def apply(start: Vertex, cutoffPred: Vertex => Boolean, region: TraverseRegion): Traverser = {
    Traverser(start, TraverserCutOff(cutoffPred, region))
  }

  def apply(start: Vertex,
            cutoff: TraverserCutOff,
            maxHops: Int) = {
    if (maxHops < 0) {
      new Traverser(start, None, cutoff)
    } else {
      new Traverser(start, Some(maxHops), cutoff)
    }
  }

  def cell(root: Vertex) = Traverser(root, TraverserCutOff.cell(root))
}
