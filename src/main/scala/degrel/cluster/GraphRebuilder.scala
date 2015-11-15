package degrel.cluster

import degrel.core._

import scala.collection.mutable

class GraphRebuilder(val source: DGraph, val node: LocalNode) {

  val headers = mutable.HashMap[ID, VertexHeader](source.vertices.map { dv =>
    dv.id -> mkHeader(dv)
  }: _*)

  def mkHeader(dv: DVertex): VertexHeader = dv match {
    case _: DCell => new CellHeader(null, dv.id)
    case _: DRule => new RuleVertexHeader(null, dv.id)
    case _ => new LocalVertexHeader(null, dv.id)
  }

  def mkBody(dv: DVertex): VertexBody = {
    def mkEdges(): Seq[Edge] = {
      dv.edges.map(concreteEdges)
    }

    def mkAttrs(): Map[Label, String] = dv.attributes match {
      case null => Map()
      case attributes => attributes.map {
        case (k, v) => Label(k) -> v
      }.toMap
    }
    dv match {
      case _: DCell => CellBody(mkEdges())
      case DRule(_, lhs, rhs, preds, pragma, _) => new RuleVertexBody(
        fetchHeader(lhs),
        fetchHeader(rhs),
        preds.map(fetchHeader),
        pragma.map(concreteEdges))
      case _ => new LocalVertexBody(dv.label, mkAttrs(), mkEdges())
    }
  }

  def fetchHeader(id: ID): VertexHeader = {
    headers.getOrElseUpdate(id, new RemoteVertexHeader(id, node))
  }

  def concreteEdges(de: DEdge): Edge = {
    Edge(null, Label(de.label), fetchHeader(de.dst))
  }

  // concreteVertices
  source.vertices.foreach { dv =>
    headers(dv.id).write(mkBody(dv))
  }

  def get(id: ID): Option[Vertex] = {
    headers.get(id)
  }
}

object GraphRebuilder {
  private val CELL_LABEL = SpecialLabels.V_CELL.name
}
