package degrel.graphbuilder

import degrel.core.{Vertex, VertexHeader}
import degrel.front.AstValueVertex

class ValueVertexBuilder(val parent: Primitive, val ast: AstValueVertex[_]) extends Builder[Vertex] {
  override def variables: LexicalSymbolTable = parent.variables

  override def children: Iterable[Primitive] = Seq()

  override def doBuildPhase(phase: BuildPhase): Unit = {}

  override def outerCell: CellBuilder = parent.outerCell

  override def header: Vertex = VertexHeader(ast.toVertex)
}
