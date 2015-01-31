package degrel.misc.serialize

/**
 * `degrel.core`以下のグラフ表現を，シリアライズしやすくするために
 * 木構造へ変更します．(.NET Coreの`System.Xml.XDocument`を参考にしています)
 *
 * グラフを木構造へ変換する上で問題になるのは，巡回している接続のが問題になります
 * `DDocument`では`DRef`を頂点への参照として用意していて，`DVertex` + `DEdge`による
 * 木構造と`DRef`によるバックリンクに変換します．
 *
 * `DVertex`には`DDocument`内で一意のIDを保持しており，`DRef`では
 * それを用いて参照先の`DVertex`を指定します．そしてその参照のマッピングは
 * `DDocument`内で保持され，`DDocument.lookup`を経由して参照できます
 */
trait DDocument {
  val vertices: Seq[DVertex]

  def lookup(id: DNodeID): Option[DVertex] = idTable.get(id)

  protected lazy val idTable = this.vertices.map(node => node.id -> node).toMap
}
