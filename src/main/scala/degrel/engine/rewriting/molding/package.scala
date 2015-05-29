package degrel.engine.rewriting

import degrel.core.Vertex
import degrel.engine.Driver

/**
 * パターンマッチしたデータとパターンの対応表と，生成するグラフのパターンを受け取り，
 * 書き換え後のグラフを生成します
 * <ul>
 *   <li>データグラフ: パターンマッチの対象となったグラフ</li>
 *   <li>パターングラフ: パターンマッチに使ったパターン．ルール左辺のグラフ</li>
 *   <li>モールドグラフ: 書き換えで書き込むグラフ．ルールの右辺のグラブ</li>
 * </ul>
 *
 * {@link molding.mold}
 */
package object molding {

  /**
   * モールドを実行します
   * @param mold モールドグラフ
   * @param binding 頂点対応表
   * @param molderFactory `Mold Vertex`に対してどの`Molder`を使用するか決定する`Abstract Factory`
   * @return 生成されたグラフの根
   */
  def mold(mold: Vertex, binding: Binding, driver: Driver)
          (implicit molderFactory: MolderFactory = MolderFactory.default): Vertex = {
    val context = new MoldingContext(binding, molderFactory, driver)
    val rootMolder = context.getMolder(mold)
    Molder.phases.foreach(rootMolder.process(_))
    rootMolder.header
  }
}
