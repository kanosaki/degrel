package degrel.graphbuilder

import degrel.core.Vertex
import degrel.utils.TreeMap

trait LexicalSymbolTable extends TreeMap[String, LexicalSymbol] {
  def bind(sym: LexicalSymbol): Unit = {
    this.bindSymbol(sym.expr, sym)
  }

  def bind(expr: String, builder: Builder[Vertex], lType: LexicalType = LexicalType.Vertex): Unit = {
    this.bind(LexicalSymbol(expr, builder, lType))
  }

  def resolveTyped(key: String, typ: LexicalType): List[List[Builder[Vertex]]] = {
    this.resolveGrouped(key)
      .map(
        _.filter(_.symType == typ)
          .map(_.builder)
      )
  }

  def lookupTyped(key: String, typ: LexicalType): LookupResult[Builder[Vertex]] = {
    this.resolveTyped(key, typ) match {
      case List(res) :: Nil => LookupResult.UniqueTopLevel(res)
      case others => others.filter(_.nonEmpty) match {
        case Nil => LookupResult.NotFound()
        case List(res) :: tail if tail.flatten.isEmpty => LookupResult.UniqueAny(res)
        case _ if others.forall(_.size == 1) => LookupResult.FoundOrdered(others.flatten)
        case _ => throw new Exception(s"Duplicated definition in $this")
      }
    }
  }
}

object LexicalSymbolTable {
  def empty: LexicalSymbolTable = {
    new RootLexicalSymbolTable()
  }
}

class ScopeSymbolTable(val parent: LexicalSymbolTable) extends LexicalSymbolTable {

}

class RootLexicalSymbolTable extends LexicalSymbolTable {
  override protected val parent: LexicalSymbolTable = null
}


