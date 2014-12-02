package degrel.graphbuilder

import degrel.core.{Cell, CellHeader}
import degrel.front.AstCell

class CellBuilder(val parent: Primitive,
                  val ast: AstCell) extends Builder[Cell] {

  val header = new CellHeader(null)

  override def variables: LexicalVariables = ???

  override def outerCell: CellBuilder = ???

  override def concrete(): Unit = {

  }

  override def children: Iterable[Primitive] = ???
}
