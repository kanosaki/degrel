package degrel

import degrel.core.{Cell, Vertex}
import degrel.front.{Ast, AstVertex, AstGraph}

/**
 * ASTからグラフを生成します
 */
package object graphbuilder {
  def build[T <: Vertex](ast: AstGraph[T])
                        (implicit factory: BuilderFactory = BuilderFactory.default): T = {
    val builder = factory.get(null, ast)
    builder.get()
  }

  def build(ast: Ast): Cell = build(ast.root)

  def validate(ast: AstVertex) = ???

  type Primitive = Builder[Vertex]
}
