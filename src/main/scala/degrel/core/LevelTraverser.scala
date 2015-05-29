package degrel.core


import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class LevelTraverser(start: Vertex, maxHop: Int) extends Iterable[Iterable[Vertex]] {
  require(maxHop >= 0)

  override def iterator: Iterator[Iterable[Vertex]] = new Itor()


  class Itor extends Iterator[Iterable[Vertex]] {
    protected val vHistory = new mutable.HashSet[Vertex]()
    protected var nextVertices = new ListBuffer[Vertex]
    protected var currentVertices = new ListBuffer[Vertex]
    protected var currentLevel = 0

    nextVertices += start
    this.pullNextLevel()

    override def hasNext: Boolean = currentVertices.nonEmpty

    def pullNextLevel() = {
      if (currentLevel < maxHop) {
        val newHopItems = new ListBuffer[Vertex]
        val candidates = this.nextVertices
          .flatMap(_.edges.map(_.dst).filter(e => !vHistory.contains(e)))
        // 同レベルに同じ物が来るのを防ぐために，頭から一つづつvHistoryへ入れてチェックします
        for(candidate <- candidates) {
          if (!vHistory.contains(candidate)) {
            newHopItems += candidate
            vHistory += candidate
          }
        }
        this.currentVertices = this.nextVertices
        this.nextVertices = newHopItems
      } else {
        this.currentVertices = this.nextVertices
        this.nextVertices = new ListBuffer[Vertex]()
      }
      currentLevel += 1
    }

    override def next(): Seq[Vertex] = {
      val current = this.currentVertices
      this.pullNextLevel()
      current
    }

  }
}

object LevelTraverser {
  def apply(v: Vertex, maxHop: Int) = {
    new LevelTraverser(v, maxHop)
  }
}
