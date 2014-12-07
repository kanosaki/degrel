package degrel.graphbuilder

import degrel.core.{Vertex, VertexHeader}

/**
 * グラフは巡回するデータ構造のため，ヘッダ部とボディ部に分かれています．
 * まずすべての頂点のヘッダ部のみ作成され，その後に作成されたヘッダ部に向かい
 * 接続が作成され，それがボディ部となります
 */
trait Builder[+T <: Vertex] {

  /**
   * このグラフ要素における環境
   */
  def variables: LexicalVariables

  /**
   * このグラフ要素を直接内包するCell
   */
  def outerCell: CellBuilder

  /**
   * 親となるGraphBuilder
   */
  def parent: Primitive

  def children: Iterable[Primitive]

  /**
   * このグラフ要素への参照用のヘッダ
   */
  def header: T

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   */
  def concrete(): Unit

  /**
   * ボディ部が作成済みかどうかを返します
   */
  def isConcreted: Boolean = _isConcreted

  def isConcreted_=(value: Boolean): Boolean = {
    _isConcreted = value
    _isConcreted
  }

  protected var _isConcreted = false

  def concreteAll(): Unit = {
    if(!this.isConcreted) {
      this.concrete()
      this.isConcreted = true
      this.children.foreach(_.concreteAll())
    }
  }

  /**
   * ボディ部を返します，{@code header}を直接参照する場合と違うことは
   * まだconcreteが行われていない場合は，自動的にconcreteが実行されます
   */
  def get(): T = {
    if (!this.isConcreted) {
      this.concrete()
      this.isConcreted = true
    }
    this.header
  }

  def factory: BuilderFactory = parent match {
    case null => throw new RuntimeException(s"BuildefFactory for ${this} undefined!")
    case _ => parent.factory
  }
}

object Builder {
  def empty: Primitive = new BuilderRoot()

}

class BuilderRoot extends Primitive {
  protected val defaultFactory = new BuiltinBuilderFactory()

  override def outerCell: CellBuilder = null // null?

  override def variables: LexicalVariables = LexicalVariables.empty

  override def parent = null

  override def header: Vertex = new VertexHeader(null)

  override def isConcreted: Boolean = false

  override def concrete(): Unit = ???

  override def children: Iterable[Primitive] = ???

  override def factory: BuilderFactory = defaultFactory
}
