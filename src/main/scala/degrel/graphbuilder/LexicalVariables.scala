package degrel.graphbuilder

import degrel.core.Vertex
import degrel.front.{AstCell, AstLinerExpr}
import degrel.utils.TreeMap

trait LexicalVariables extends TreeMap[String, Builder[Vertex]] {

}

object LexicalVariables {
  def empty: LexicalVariables = {
    new EmptyBinding()
  }
}

/**
 * {@code LexicalVariables}はチェインするので，その起点となるオブジェクト
 */
class EmptyBinding extends LexicalVariables {
  override def resolve(expr: String): List[Builder[Vertex]] = {
    Nil
  }

  override protected val parent: TreeMap[String, Builder[Vertex]] = null
}

/**
 * ルール左辺のコンテキスト．
 * ルール左辺では最初に変数の束縛が行われた後にグラフの構築が行われるが，
 * すでに束縛済みの場合は新規にグラフを構成するのではなく，束縛として渡したグラフを渡す必要がある．
 * これは束縛と実際に構成されるグラフのインスタンスに一貫性を持たせるためである
 * @example "A[foo](..)"という頂点が左辺に現れた場合，最初にLhsContextにシンボルと対応する頂点"A" -> foo(...)が登録される
 *          その後，グラフを構成するときに，変数が頂点に存在する場合は，LhsContextからシンボルを取得しそれを使用する必要がある
 * @param parent
 */
class ExprBinding(val parent: LexicalVariables, exprAst: AstLinerExpr)
  extends LexicalVariables {

}


class CellBinding(val parent: LexicalVariables, cellAst: AstCell)
  extends LexicalVariables {

}

