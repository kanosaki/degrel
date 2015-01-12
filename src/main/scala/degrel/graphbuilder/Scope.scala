package degrel.graphbuilder

import degrel.core.Vertex

class Scope(val parent: Primitive) extends Primitive {
  /**
   * このグラフ要素における環境
   */
  override val variables: LexicalVariables = new ScopeVariables(parent.variables)

  /**
   * 自分の子の{@code Builder[T]}
   * @return
   */
  override def children: Iterable[Primitive] = parent.children

  /**
   * このメソッドが呼ばれると，ボディ部を作成します．
   * 作成されたボディ部はヘッダを経由して使用するため，直接取得は出来ません
   * @note childrenに登録された子Builder[T]への再帰的concreteは
   *       concreteAllによって自動的に処理されるため実装する必要はありません
   */
  override def doBuildPhase(phase: BuildPhase): Unit = parent.doBuildPhase(phase)

  /**
   * このグラフ要素を直接内包するCell
   */
  override def outerCell: CellBuilder = parent.outerCell

  /**
   * このグラフ要素への参照用のヘッダ
   */
  override def header: Vertex = parent.header
}
