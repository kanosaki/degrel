package degrel.utils


import degrel.core.Label
import degrel.{core, front}

import scala.language.implicitConversions

object FlyWrite {

  def vHead(s: String, attributes: Map[Label, String] = Map()) = {
    core.Vertex(s, List(), Label.convertAttrMap(attributes))
  }

  def vAll(s: String, attributes: Map[Label, String], edges: Iterable[core.Edge]) = {
    core.Vertex(s, edges, Label.convertAttrMap(attributes))
  }

  implicit def flywriteCoreStringExtension(s: String): StringExtensions = new StringExtensions(s)

  implicit def flywriteCoreVertexExtension(v: core.Vertex): VertexExtensions = new VertexExtensions(v)

  class StringExtensions(s: String) {
    def |^|(edges: Iterable[core.Edge]) = {
      core.Vertex(s, edges)
    }

    def |^|(edges: Product) = {
      core.Vertex.create(s) { v =>
        edges.productIterator.collect { case e: core.Edge => e}.toSeq
      }
    }

    def |^|(edge: core.Edge) = {
      core.Vertex(s, List(edge))
    }

    def v() = {
      core.Vertex(s, List())
    }

    def |:|(v: core.Vertex) = {
      core.Edge(null, core.Label(s), v)
    }

  }

  class VertexExtensions(v: core.Vertex) {
    def |->|(rhs: core.Vertex) = {
      core.Vertex.create(core.SpecialLabels.V_RULE) { src =>
        Seq(core.Edge(src, core.Label(front.SpecialLabel.Edge.lhs), v),
          core.Edge(src, core.Label(front.SpecialLabel.Edge.rhs), rhs))
      }
    }

  }

}

