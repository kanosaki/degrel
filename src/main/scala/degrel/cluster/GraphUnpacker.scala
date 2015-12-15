package degrel.cluster

import degrel.core._

import scala.collection.mutable

class GraphUnpacker(val source: DGraph, val node: LocalNode, idSpace: IDSpace) {
  val originalIDMap = source.idMap.toMap

  def assignID(did: DID): ID = {
    originalIDMap.getOrElse(did, idSpace.next())
  }

  val headers = mutable.HashMap[DID, VertexHeader](source.vertices.map { dv =>
    dv.id -> mkHeader(dv)
  }: _*)

  def mkHeader(dv: DVertex): VertexHeader = dv match {
    case _: DCell => new CellHeader(null, assignID(dv.id))
    case _: DRule => new RuleVertexHeader(null, assignID(dv.id))
    case _ => new LocalVertexHeader(null, assignID(dv.id))
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

  def fetchHeader(id: DID): VertexHeader = {
    // do not use `assignID` here, `assignID` might assign new local ID. but
    // `RemoteVertexHeader` should have valid remote ID!
    headers.getOrElseUpdate(id, new RemoteVertexHeader(originalIDMap(id), node))
  }

  def concreteEdges(de: DEdge): Edge = {
    Edge(null, Label(de.label), fetchHeader(de.dst))
  }

  // concreteVertices
  source.vertices.foreach { dv =>
    headers(dv.id).write(mkBody(dv))
  }

  def get(id: DID): Option[Vertex] = {
    headers.get(id)
  }

  def root: VertexHeader = headers(0)
}

object GraphUnpacker {
  private val CELL_LABEL = SpecialLabels.V_CELL.name
}
