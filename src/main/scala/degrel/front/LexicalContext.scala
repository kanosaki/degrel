package degrel.front

import scala.collection.mutable
import degrel.core

class NameError(expr: String) extends Exception {

}

trait LexicalContext {
  val parent: LexicalContext
  def isPattern = false

  protected val symbolMap: mutable.MultiMap[String, Any] =
    new mutable.HashMap[String, mutable.Set[Any]] with mutable.MultiMap[String, Any]

  def resolve(expr: String) : List[Any] = {
    resolveInThis(expr) :: parent.resolve(expr)
  }

  def resolveExact[T](expr: String) : T = {
    this.resolve(expr) match {
      case (value: T) :: Nil => value
      case _ => throw new NameError(expr)
    }
  }

  protected def resolveInThis(expr: String): List[Any] = {
    symbolMap(expr).toList
  }
}

class FileContext(val parent: LexicalContext) extends LexicalContext {

}

class RhsContext(val parent: LexicalContext)(captures: List[(String, core.Vertex)]) extends LexicalContext {

}

class LhsContext(val parent: LexicalContext) extends LexicalContext {
  override def isPattern = true
}
