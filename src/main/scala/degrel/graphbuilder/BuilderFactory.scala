package degrel.graphbuilder

import degrel.core.Vertex
import degrel.front.{AstBinExpr, AstFunctor, AstCell, AstGraph}

/**
 * どのようなBuilder[T]を使用してASTをぐらふに変換するかを制御します
 */
trait BuilderFactory {
  def get[T <: Vertex](parent: Primitive, ast: AstGraph[T]): Builder[T]
}

object BuilderFactory {
  val default = new BuiltinBuilderFactory()
}

class BuiltinBuilderFactory extends BuilderFactory {
  override def get[T <: Vertex](parent: Primitive, ast: AstGraph[T]): Builder[T] = ast match {
    case cell: AstCell => new CellBuilder(parent, cell)
    case expr: AstBinExpr => new ExprBuilder(parent, expr)
    case functor: AstFunctor => new FunctorBuilder(parent, functor)
    case _ => throw new BuilderException(s"Cannot found builder for $ast")
  }
}

