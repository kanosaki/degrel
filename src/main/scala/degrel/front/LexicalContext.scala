package degrel.front

import scala.collection.mutable
import scala.reflect.ClassTag
import degrel.core

class NameError(expr: String) extends Exception {
  override def toString = {
    expr
  }
}

trait LexicalContext {
  protected val parent: LexicalContext
  def isPattern = false

  protected val symbolMap: mutable.MultiMap[String, Any] =
    new mutable.HashMap[String, mutable.Set[Any]] with mutable.MultiMap[String, Any]

  def resolve(expr: String) : List[Any] = {
    resolveInThis(expr) ++ parent.resolve(expr)
  }

  def resolveExact[T : ClassTag](expr: String) : T = {
    val klass = implicitly[ClassTag[T]].runtimeClass
    this.resolve(expr) match {
      case value :: Nil if klass.isInstance(value) => value.asInstanceOf[T]
      case _ => throw new NameError(expr)
    }
  }

  protected def resolveInThis(expr: String): List[Any] = {
    symbolMap.get(expr) match {
      case Some(vs) => vs.toList
      case None => List()
    }
  }
}

object LexicalContext {
  def empty : LexicalContext = {
    new RootContext()
  }
}

class RootContext extends LexicalContext {
  val parent: LexicalContext = null

  override def resolve(expr: String) : List[Any] = {
    Nil
  }
}

class FileContext(val parent: LexicalContext) extends LexicalContext {

}

class RhsContext(val parent: LexicalContext)(captures: List[(String, core.Vertex)]) extends LexicalContext {
  for((s, v) <- captures)
    symbolMap.addBinding(s, v)
}

class LhsContext(val parent: LexicalContext) extends LexicalContext {
  private val captureCache = new mutable.HashMap[AstVertex, core.Vertex]
  override def isPattern = true

  def fromCaptureCache(vertex: AstVertex) : Option[core.Vertex] = {
    captureCache.get(vertex)
  }

  def storeCaptureCache(astV: AstVertex, coreV : core.Vertex) {
    captureCache += (astV -> coreV)
  }
}
