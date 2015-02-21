package degrel.graphbuilder

import degrel.core.{VertexBody, Vertex, VertexHeader}

/**
 * グラフは巡回するデータ構造のため，ヘッダ部とボディ部に分かれています．
 * まずすべての頂点のヘッダ部のみ作成され，その後に作成されたヘッダ部に向かい
 * 接続が作成され，それがボディ部となります
 */
trait Builder[+T <: Vertex] {

  /**
   * このグラフ要素における環境
   */
  def variables: LexicalSymbolTable

  /**
   * このグラフ要素を直接内包するCell
   */
  def outerCell: CellBuilder

  /**
   * 親となるGraphBuilder
   */
  def parent: Primitive

  /**
   * 自分の子の{@code Builder[T]}
   * @return
   */
  def children: Iterable[Primitive]

  /**
   * このグラフ要素への参照用のヘッダ
   */
  def header: T

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   * @note childrenに登録された子Builder[T]への再帰的concreteは
   *       concreteAllによって自動的に処理されるため実装する必要はありません
   */
  def doBuildPhase(phase: BuildPhase): Unit

  protected var phase: BuildPhase = NothingDone

  def doBuildPhaseRecursive(phase: BuildPhase): Unit = {
    this.phase = phase
    this.doBuildPhase(phase)
    this.children.foreach(ch => {
      ch.doBuildPhaseRecursive(phase)
    })
  }

  /**
   * ボディ部を返します，{@code header}を直接参照する場合と違うことは
   * まだconcreteが行われていない場合は，自動的にconcreteが実行されます
   */
  def get(): T = {
    Builder.buildSequence.foreach(phase => {
      this.doBuildPhaseRecursive(phase)
    })
    this.header
  }

  /**
   * この{@code Builder[T]}における{@code BuildefFactory}を返します
   */
  def factory: BuilderFactory = parent match {
    case null => throw new RuntimeException(s"BuildefFactory for ${this} undefined!")
    case _ => parent.factory
  }
}

object Builder {
  def empty: Primitive = new BuilderRoot()

  def buildSequence = Seq(MainPhase, FinalizePhase)
}

/**
 * Builter[T]の起点となるクラス，通常は{@code Builder.empty}を経由して使用してください
 */
class BuilderRoot extends Primitive {
  protected val defaultFactory = new BuiltinBuilderFactory()

  override def outerCell: CellBuilder = null // TODO: null?

  override val variables: LexicalSymbolTable = LexicalSymbolTable.empty

  override def parent = null

  override def header: Vertex = new VertexHeader(null)

  override def children: Iterable[Primitive] = Seq()

  override def factory: BuilderFactory = defaultFactory

  override def doBuildPhase(phase: BuildPhase): Unit = {}
}
