package degrel.core

import scala.collection.mutable

class CellBody(initRoots: Iterable[Vertex],
               initRules: Iterable[Vertex],
               initBases: Iterable[Vertex],
               val otherEdges: Seq[Edge]) extends VertexBody with Cell {
  private lazy val _rules = mutable.ListBuffer(initRules.map(_.asRule).toSeq: _*)
  private lazy val _roots = mutable.ListBuffer(initRoots.toSeq: _*)

  override def rules: Seq[Rule] = _rules

  override def roots: Seq[Vertex] = _roots

  override def edges: Iterable[Edge] =
    roots.map(Edge(this, Label.E.cellItem, _)) ++
      rules.map(Edge(this, Label.E.cellRule, _)) ++
      bases.map(Edge(this, Label.E.cellBase, _)) ++
      otherEdges

  override def attributes: Map[Label, String] = Map()

  override def shallowCopy(): Vertex = new CellBody(roots, rules, bases, otherEdges)

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

  override def asCell: Cell = this

  override def asCellBody: CellBody = this

  /**
   * このCellを直接内包するCell
   */
  override def parent: Cell = ???

  /**
   * この`Cell`の元になるCell．
   * 規則を継承します
   */
  override lazy val bases: Seq[Cell] = initBases.map(_.unref[Cell]).toSeq
}

object CellBody {
  def apply(edges: Iterable[Edge]) = {
    val roots = edges.filter(_.label == Label.E.cellItem).map(_.dst)
    val rules = edges.filter(_.label == Label.E.cellRule).map(_.dst)
    val bases = edges.filter(_.label == Label.E.cellBase).map(_.dst).toSeq
    val others = edges.filter(l => l.label != Label.E.cellItem && l.label != Label.E.cellRule && l.label != Label.E.cellBase).toSeq
    new CellBody(roots, rules, bases, others)
  }
}
