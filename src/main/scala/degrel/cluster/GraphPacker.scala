package degrel.cluster

import degrel.core._

import scala.collection.mutable

class GraphPacker(root: Vertex, move: Boolean = false) {
  require(root != null)

  private var currentID: DID = -1
  val idMapping = mutable.HashMap[ID, DID]()
  val vertices = mutable.ListBuffer[DVertex]()
  this += root

  private def nextID(): DID = {
    currentID += 1
    currentID
  }

  def assignDID(v: Vertex): DID = {
    idMapping.getOrElseUpdate(v.id, nextID())
  }

  def +=(v: Vertex): this.type = {
    assignDID(v)
    v.neighbors.foreach(this.assignDID)
    vertices += mapToDElement(v)
    this
  }

  def ++=(vertices: Iterable[Vertex]): this.type = {
    vertices.foreach(+=)
    this
  }

  def pack(): DGraph = {
    if (move) {
      DGraph(0, vertices.toVector, Vector())
    } else {
      DGraph(0, vertices.toVector, idMapping.map(_.swap).toVector)
    }
  }

  protected def mapToDElement(v: Vertex): DVertex = {
    def dEdges(): Seq[DEdge] = {
      v.edges.map(mapEdges).toVector
    }
    def dAttrs(): Seq[(String, String)] = {
      v.attributes.map {
        case (k, v) => k.expr -> v
      }.toVector
    }

    v.label match {
      case Label.V.rule => {
        val (lhs, rhs, preds, pragmaEdges) = Rule.splitEdges(v.edges)
        DRule(assignDID(v),
              assignDID(lhs),
              assignDID(rhs),
              preds.map(assignDID),
              pragmaEdges.map(mapEdges).toVector, dAttrs())
      }
      case Label.V.cell => DCell(assignDID(v), dAttrs(), dEdges(), Seq())
      case _ => DPlainVertex(assignDID(v), v.label.expr, dAttrs(), dEdges())
    }
  }

  protected def mapEdges(e: Edge): DEdge = {
    DEdge(e.label.expr, assignDID(e.dst))
  }
}
