package degrel.front.graphbuilder

import degrel.core.{Label, Vertex, VertexHeader}
import degrel.front.AstValueVertex

class ValueVertexBuilder(val parent: Primitive, val ast: AstValueVertex[_]) extends Builder[Vertex] {
  override def variables: LexicalSymbolTable = parent.variables

  override def children: Iterable[Primitive] = Seq()

  override def doBuildPhase(phase: BuildPhase): Unit = {}

  override def ownerCell: CellBuilder = parent.ownerCell

  override def header: Vertex = VertexHeader(ast.toVertex)

  override def typeLabel: Option[Label] = Some(Label.V.value)
}
