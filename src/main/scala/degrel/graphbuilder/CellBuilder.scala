package degrel.graphbuilder

import degrel.core._
import degrel.front.AstCell

class CellBuilder(val parent: Primitive,
                  val ast: AstCell) extends Builder[Cell] {
  val header = new CellHeader(null)
  override val variables: LexicalSymbolTable = new CellSymbolTable(parent.variables)

  override val children = ast.roots.map(a => factory.get(this, a))

  override def outerCell: CellBuilder = parent.outerCell

  override def doBuildPhase(phase: BuildPhase): Unit = phase match {
    case MainPhase => {
    }
    case FinalizePhase => {
      val rules = children.filter(_.header.isInstanceOf[Rule])
      val roots = children.filter(!_.header.isInstanceOf[Rule])
      val ruleEdges = rules.map(b => Edge(this.header, SpecialLabels.E_CELL_RULE, b.header))
      val rootEdges = roots.map(b => Edge(this.header, SpecialLabels.E_CELL_ITEM, b.header))
      val body = new CellBody(rootEdges ++ ruleEdges)
      header.write(body)
    }
    case _ =>
  }
}

class CellSymbolTable(val parent: LexicalSymbolTable) extends LexicalSymbolTable {
}
