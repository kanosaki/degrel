package degrel

import degrel.core.Vertex

/**
 * フロントエンド
 * テキストデータから抽象構文木を構築します
 */
package object front {
  type AstVertex = AstExpr[Vertex]
}
