package degrel.core

import scala.collection.mutable

class CellBody(initRoots: Iterable[Vertex], initRules: Iterable[Rule]) extends VertexBody with Cell {
  private val _rules = mutable.ListBuffer(initRules.toSeq: _*)
  private val _roots = mutable.ListBuffer(initRoots.toSeq: _*)

  override def rules: Seq[Rule] = _rules

  override def roots: Seq[Vertex] = _roots

  override def edges: Iterable[Edge] =
    roots.map(Edge(this, Label.E.cellItem, _)) ++
    rules.map(Edge(this, Label.E.cellRule, _))

  override def attributes: Map[Label, String] = Map()

  override def shallowCopy(): Vertex = new CellBody(roots, rules)

  override def label: Label = Label.V.cell

  override def removeRoot(v: Vertex): Unit = {
    _roots -= v
  }

  override def addRoot(v: Vertex): Unit = {
    if (v.isRule) {
      _rules += v.asRule
    } else {
      _roots += v
    }
  }
}

object CellBody {
  def apply(edges: Iterable[Edge]) = {
    val rules = edges.filter(_.label == Label.E.cellRule).map(_.dst.asRule)
    val roots = edges.filter(_.label == Label.E.cellItem).map(_.dst)
    new CellBody(roots, rules)
  }
}
