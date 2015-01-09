package degrel.graphbuilder

import degrel.core.Vertex
import degrel.front.{AstCell, AstLinerExpr}
import degrel.utils.{TreeMapRoot, TreeMap}

trait LexicalVariables extends TreeMap[String, Builder[Vertex]] {

}

object LexicalVariables {
  def empty: LexicalVariables = {
    new RootLexicalVariables()
  }
}

class RootLexicalVariables extends TreeMap[String, Builder[Vertex]] with LexicalVariables {
  override protected val parent: TreeMap[String, Builder[Vertex]] = null
}


