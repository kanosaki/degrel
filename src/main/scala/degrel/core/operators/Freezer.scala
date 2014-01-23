package degrel.core.operators


import degrel.core._
import scala.collection.mutable
import scalaz.Free
import java.util.NoSuchElementException

class Freezer(val root: Vertex) {
  val traverser = Traverser(root)
  val vMap = new mutable.HashMap[Vertex, VertexBody]()
  val builtVertices = new mutable.ListBuffer[VertexBody]()

  for (v <- traverser) {
    val built = buildVertex(v)
    vMap += v -> built
    builtVertices += built
  }

  val builtRoot = vMap(root)


  private def buildVertex(v: Vertex): VertexBody = {
    val edges = v.edges().map(e => {
      Edge(null, e.label, {vMap(e.dst)})
    })
    VertexBody(v.label, v.attributes, edges, ID.NA)
  }

  def freeze: Vertex = builtRoot
}

object Freezer {
  def apply(vertex: Vertex) = {
    val freezer = new Freezer(vertex)
    freezer.freeze
  }
}
