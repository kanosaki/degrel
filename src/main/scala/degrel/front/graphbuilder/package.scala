package degrel

import degrel.core.{Cell, Vertex}
import degrel.front.{Ast, AstVertex, AstGraph}

/**
 * ASTからグラフ(degrel.core)による表現に変形を行います
 *
 * 重要なインターフェイスは2つで，`Builder[T]`トレイトと`BuilderFactory`トレイトです
 * `Builder[T]`は実際にグラフを構築するオブジェクトで，基本的にコンストラクタでASTのインスタンスを
 * 受け取り，それに応じてグラフを構築します．
 * `BuilderFactory`は，ASTのインスタンスと，それに対応した`Builder[T]`を生成するためのクラスです
 * なぜ`BuilderFactory`が必要かというと，DEGRELではすべてはグラフであるため頂点と接続でデータが表現されますが
 * 実際には最適化のため，書き換え規則やCellは特別扱いされます．具体的には特別なクラスが生成されます．
 * 例えば，ラベルが`->`で接続`lhs`, `rhs`を持つような頂点は書き換え規則であるはずなので，
 * 一般的な頂点を構築する`FunctorBuilder`ではなく，`RuleBuilder`を用いる必要があります．
 * このような分岐を一手に引き受けるのが`BuilderFactory`です．
 *
 * グラフ構築は複数のフェーズに分かれて行われ，
 * それぞれのフェーズで順番に構築が行われていきます．グラフは巡回を持つデータ構造なので
 * 再帰呼び出し等々で一度にすべての参照を解決するのは難しいので，数度に分けて構築します．
 * それぞれのフェーズで何をするのか，何をしてはいけないのか等々は`BuilderPhase`のインスタンスの
 * ドキュメントを参照してください
 */
package object graphbuilder {
  def build[T <: Vertex](ast: AstGraph[T])
                        (implicit factory: BuilderFactory = BuilderFactory.default): T = {
    val builder = factory.get(factory.createRoot, ast)
    builder.get()
  }

  def build(ast: Ast): Vertex = build(ast.root)

  def validate(ast: AstVertex) = ???

  type Primitive = Builder[Vertex]
}
