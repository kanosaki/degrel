package degrel.front.graphbuilder

import degrel.core._
import degrel.front.{AstLabel, AstCell}

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
      val rules = rootChildren.collect {
        case r if r.header.isInstanceOf[Rule] => r.header.asInstanceOf[Rule]
      }
      val roots = rootChildren.collect {
        case r if !r.header.isInstanceOf[Rule] => r.header
      }
      val bases = edgeChildren.collect {
        case (AstLabel(lbl), builder) if lbl == Label.E.cellBase.expr => {
          builder.header
        }
      }
      val others = edgeChildren.collect {
        case (AstLabel(l), builder) if l != Label.E.cellBase.expr => {
          Edge(this.header, Label(l), builder.header)
        }
      }
      val body = CellBody(roots, rules, bases, others)
      header.write(body)
    }
    case _ =>
  }
}

class CellSymbolTable(val parent: LexicalSymbolTable) extends LexicalSymbolTable {
}
