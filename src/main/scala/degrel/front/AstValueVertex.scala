package degrel.front

import degrel.core.{ObjectVertex, ValueVertex}

trait AstValueVertex[T] extends AstVertex {
  def toVertex: ValueVertex[T]
}

case class AstIntegerVertex(expr: String) extends AstValueVertex[Int] {
  override def toVertex: ValueVertex[Int] = {
    ValueVertex(expr.toInt)
  }
}

case class AstFloatVertex(expr: String) extends AstValueVertex[Double] {
  override def toVertex: ValueVertex[Double] = {
    ValueVertex(expr.toDouble)
  }
}

case class AstStringVertex(expr: String) extends AstValueVertex[String] {
  override def toVertex: ValueVertex[String] = {
    ValueVertex(expr)
  }
}
