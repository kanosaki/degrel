package degrel.utils


import scala.language.implicitConversions
import degrel.core
import degrel.front

object FlyWrite {

  class StringExtensions(s: String) {
    def |^|(edges: Iterable[core.Edge]) = {
      core.Vertex(s, edges)
    }

    def |^|(edges: Product) = {
      core.Vertex(s, edges.productIterator.collect {case e: core.Edge => e}.toSeq)
    }

    def |^|(edge: core.Edge) = {
      core.Vertex(s, List(edge))
    }

    def |^|(v: Unit) = {
      core.Vertex(s, List())
    }

    def |:|(v: core.Vertex) = {
      core.Edge(core.Label(s), v)
    }

  }

  class VertexExtensions(v: core.Vertex) {
    def |->|(rhs: core.Vertex) = {
      core.Vertex(front.SpecialLabel.Vertex.rule,
                  Seq(core.Edge(core.Label(front.SpecialLabel.Edge.lhs), v).freeze,
                      core.Edge(core.Label(front.SpecialLabel.Edge.rhs), rhs).freeze))
    }

  }

  def vHead(s: String, attributes: Map[String, String] = Map()) = {
    core.Vertex(s, List(), attributes)
  }

  def vAll(s: String, attributes: Map[String, String], edges: Iterable[core.Edge]) = {
    core.Vertex(s, edges, attributes)
  }

  implicit def flywriteCoreStringExtension(s: String) = new StringExtensions(s)

  implicit def flywriteCoreVertexExtension(v: core.Vertex) = new VertexExtensions(v)
}

