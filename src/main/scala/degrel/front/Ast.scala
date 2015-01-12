package degrel.front

import degrel.core._

/**
 * 抽象構文木のコンテナクラス
 */
class Ast(val root: AstCell) {
}

/**
 * すべての抽象構文木要素の親となる型
 */
trait AstNode {

}

/**
 * グラフを生成する構文
 */
trait AstGraph[+TBuilt <: Vertex] extends AstNode {

}
