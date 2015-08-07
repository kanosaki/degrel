package degrel.front.graphbuilder

import degrel.core._
import degrel.front.AstCell

class CellBuilder(val parent: Primitive,
                  val ast: AstCell) extends Builder[Cell] {
  val header = new CellHeader(null)

  override def typeLabel: Option[Label] = Some(Label.V.cell)

  override val variables: LexicalSymbolTable = new CellSymbolTable(parent.variables)

  val rootChildren = ast.roots.map(a => factory.get(this, a))
  val edgeChildren = ast.edges.map(e => e.label -> factory.get(this, e.dst))

  override def children = rootChildren ++ edgeChildren.map(_._2)

  override def outerCell: CellBuilder = parent.outerCell

  override def doBuildPhase(phase: BuildPhase): Unit = phase match {
    case MainPhase => {
    }
    case FinalizePhase => {
      val rules = rootChildren.filter(_.header.isInstanceOf[Rule]).map(_.header.asInstanceOf[Rule])
      val roots = rootChildren.filter(!_.header.isInstanceOf[Rule]).map(_.header)
      val bases = edgeChildren.filter(_._1.expr == Label.E.cellBase.expr).map {
        case (label, builder) => {
          builder.header
        }
      }
      val others = edgeChildren.filter(_._1.expr != Label.E.cellBase.expr).map(e => Edge(this.header, Label(e._1.expr), e._2.header))
      val body = new CellBody(roots, rules, bases, others)
      header.write(body)
    }
    case _ =>
  }
}

class CellSymbolTable(val parent: LexicalSymbolTable) extends LexicalSymbolTable {
}
