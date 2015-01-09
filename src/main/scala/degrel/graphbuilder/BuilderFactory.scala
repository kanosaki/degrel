package degrel.graphbuilder

import degrel.core.Vertex
import degrel.front._

/**
 * どのようなBuilder[T]を使用してASTをぐらふに変換するかを制御します
 */
trait BuilderFactory {
  def get[T <: Vertex](parent: Primitive, ast: AstGraph[T]): Builder[T]

  def createRoot: Primitive
}

object BuilderFactory {
  val default = new BuiltinBuilderFactory()
}

class BuiltinBuilderFactory extends BuilderFactory {
  override def get[T <: Vertex](parent: Primitive, ast: AstGraph[T]): Builder[T] = ast match {
    case cell: AstCell => new CellBuilder(parent, cell)
    case binExpr: AstBinExpr => binExpr.op match {
      case BinOp.RULE => new RuleBuilder(parent, binExpr)
      case _ => new ExprBuilder(parent, binExpr)
    }
    case functor: AstFunctor => new FunctorBuilder(parent, functor)
    case _ => throw new BuilderException(s"Cannot found builder for $ast")
  }

  override def createRoot: Primitive = new BuilderRoot()
}

