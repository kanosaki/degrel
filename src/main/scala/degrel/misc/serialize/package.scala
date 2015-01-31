package degrel.misc

import degrel.core.Graph

/**
 * DEGRELの内部データをXML等の外部データへ変換するためのパッケージです．
 * 変換は4段階に分けて行われ
 * 1. Core Data (degrel.core以下のオブジェクト)
 * 2. DDocument
 * 3. 各フォーマットの中間表現 (XMLならば `scala.xml.Elem` 等)
 * 4. `String`等プリミティブなデータ
 *
 * DEGRELのデータを直接中間表現へ変換しない理由は，グラフは一度木構造へ
 * 変換しないとシリアライズ出来ないからです．どのように木構造へ変換するかは
 * `FormatFlavor`を通して指定します．
 */
package object serialize {
  type DNodeID = Long

  /**
   * データを`DDocument`へ変換します．
   * @param g
   * @param flavor
   * @return
   */
  def toDoc(g: Graph, flavor: FormatFlavor = FormatFlavor.Flat): DDocument = {
    flavor match {
      case FormatFlavor.Flat => new FlatDocument(g)
    }
  }
}
