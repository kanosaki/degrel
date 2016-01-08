package degrel.core

import degrel.engine.rewriting.Binding

import scala.collection.mutable

class CellBody(private val _roots: mutable.ListBuffer[Vertex],
               private val _rules: mutable.ListBuffer[Vertex],
               private val _bases: mutable.ListBuffer[Vertex],
               val otherEdges: Seq[Edge],
               override val binding: Binding) extends VertexBody with Cell {
  //private lazy val _rules =
  //private lazy val _roots =

  override def rules: Seq[Rule] = _rules.map(_.asRule)

  override def roots: Seq[Vertex] = _roots

  override def bases: Seq[Cell] = _bases.map(_.unref[Cell])

  override def edges: Iterable[Edge] =
    _roots.map(Edge(this, Label.E.cellItem, _)) ++
      _rules.map(Edge(this, Label.E.cellRule, _)) ++
      _bases.map(Edge(this, Label.E.cellBase, _)) ++
      otherEdges

  override def attributes: Map[Label, String] = Map()

  override def shallowCopy(): Vertex = CellBody(roots, rules, bases, otherEdges)

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
  //override lazy val bases: Seq[Cell] =
}

object CellBody {
  def apply(edges: Iterable[Edge]): CellBody = {
    val roots = edges.filter(_.label == Label.E.cellItem).map(_.dst)
    val rules = edges.filter(_.label == Label.E.cellRule).map(_.dst)
    val bases = edges.filter(_.label == Label.E.cellBase).map(_.dst).toSeq
    val others = edges.filter(l => l.label != Label.E.cellItem && l.label != Label.E.cellRule && l.label != Label.E.cellBase).toSeq
    CellBody(roots, rules, bases, others)
  }

  def apply(initRoots: Iterable[Vertex],
            initRules: Iterable[Vertex],
            initBases: Iterable[Vertex],
            otherEdges: Seq[Edge],
            binding: Binding = Binding.empty()): CellBody = {
    new CellBody(
      mutable.ListBuffer(initRoots.toSeq: _*),
      mutable.ListBuffer(initRules.toSeq: _*),
      mutable.ListBuffer(initBases.toSeq: _*),
      otherEdges,
      binding
    )
  }
}

