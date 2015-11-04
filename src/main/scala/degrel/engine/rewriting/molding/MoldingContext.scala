package degrel.engine.rewriting.molding

import degrel.core.{Edge, VertexHeader, Vertex}
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

trait MoldingContext {

  def binding: Binding

  def factory: MolderFactory

  /**
    * 現在のownerで，context.ownerMolder.header != context.driver.headerであることに注意してください
    * @return
    */
  def ownerMolder: Molder

  def ownerHeader: Vertex = {
    this.ownerMolder match {
      case null => driver.header
      case molder => molder.header
    }
  }

  /**
    * Moldingを開始したCellです
    */
  def driver: Driver

  /**
    * 現在のモールドコンテキストにおける，指定された`Vertex`を扱うための
    * `Molder`を返します．このとき，すでに対応する`Molder`が生成されている場合はキャッシュから返し，
    * そうでなければ`MolderFactory`で生成した後に返されます
    * @param mold 対象となる頂点
    * @return 渡された頂点を`Mold`として`Molding`を行う`Molder`
    */
  def getMolder(mold: Vertex): Molder

  def getHeader(mold: Vertex): VertexHeader

  /**
    * 渡されたパターン頂点とパターンマッチしているデータ頂点を返します．
    * 存在しない場合は例外を送出します
    */
  def matchedVertexExact(patternVertex: Vertex): Vertex

  /**
    * 渡されたパターン接続とパターンマッチしているデータ接続を返します．
    * 存在しない場合は例外を送出します
    */
  def matchedEdgeExact(patternEdge: Edge): Edge

  /**
    * 渡されたパターン頂点とパターンマッチしているデータ頂点が存在すれば`Option`で返します
    * @param patternVertex チェックするパターン頂点
    * @return パターンマッチしていた場合は，`Some(対応するデータ頂点)`，していなければ`None`
    */
  def matchedVertex(patternVertex: Vertex): Option[Vertex]

  /**
    * 渡されたパターン接続とパターンマッチしているデータ接続が存在すれば`Option`で返します
    * @param patternVertex チェックするパターン接続
    * @return パターンマッチしていた場合は，`Some(対応するデータ接続)`，していなければ`None`
    */
  def matchedEdge(patternEdge: Edge): Option[Edge]

  def unmatchedEdges(patternVertex: Vertex): Iterable[Edge]
}
