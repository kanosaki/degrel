package degrel.engine.rewriting.molding

import degrel.core.{Edge, Vertex, VertexHeader}
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

import scala.collection.mutable

class MoldingContextBase(override val binding: Binding,
                         override val factory: MolderFactory,
                         override val driver: Driver) extends MoldingContext {
  private[this] val molderMapping = mutable.HashMap[Vertex, Molder]()

  override def getMolder(mold: Vertex): Molder = {
    molderMapping.getOrElseUpdate(mold, factory.get(mold, this))
  }

  override def getHeader(mold: Vertex): VertexHeader = factory.getHeader(mold, this)

  override def matchedVertexExact(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternVertex not found in $binding}.")
    }
  }

  override def matchedEdgeExact(patternEdge: Edge): Edge = {
    binding.get(patternEdge) match {
      case Some(v) => v match {
        case e: Edge => e
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternEdge not found in $binding}.")
    }
  }

  override def matchedVertex(patternVertex: Vertex): Option[Vertex] = {
    binding.get(patternVertex) match {
      case Some(v) => Some(v.asInstanceOf[Vertex])
      case None => None
    }
  }

  override def matchedEdge(patternEdge: Edge): Option[Edge] = {
    binding.get(patternEdge) match {
      case Some(e) => Some(e.asInstanceOf[Edge])
      case None => None
    }
  }

  override def unmatchedEdges(patternVertex: Vertex): Iterable[Edge] = {
    binding.unmatchedEdges(matchedVertexExact(patternVertex))
  }

  override def ownerMolder: Molder = null
}
