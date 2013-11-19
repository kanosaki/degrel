package degrel.front

import scala.collection.mutable

trait LexicalContext {
  val parent: LexicalContext
  protected val symbolMap: mutable.MultiMap[Symbol, Any] =
    new mutable.HashMap[Symbol, mutable.Set[Any]] with mutable.MultiMap[Symbol, Any]

  def resolve(sym: Symbol): List[Any] = {
    resolveInThis(sym) :: parent.resolve(sym)
  }

  protected def resolveInThis(sym: Symbol): List[Any] = {
    symbolMap(sym).toList
  }
}

class FileContext(val parent: LexicalContext) extends LexicalContext {

}

class RuleContext(val parent: LexicalContext)(rule: AstRule) extends LexicalContext {
}
