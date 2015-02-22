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
      val rules = children.filter(_.header.isInstanceOf[Rule]).map(_.header.asInstanceOf[Rule])
      val roots = children.filter(!_.header.isInstanceOf[Rule]).map(_.header)
      val body = new CellBody(roots, rules)
      header.write(body)
    }
    case _ =>
  }
}

class CellSymbolTable(val parent: LexicalSymbolTable) extends LexicalSymbolTable {
}
