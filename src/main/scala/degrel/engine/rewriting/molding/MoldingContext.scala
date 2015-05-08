package degrel.engine.rewriting.molding

import degrel.core.{Cell, VertexHeader, Edge, Vertex}
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

import scala.collection.mutable

class MoldingContext(val binding: Binding, val factory: MolderFactory, val driver: Driver) {
  private[this] val molderMapping = mutable.HashMap[Vertex, Molder]()

  /**
   * 現在のモールドコンテキストにおける，指定された`Vertex`を扱うための
   * `Molder`を返します．このとき，すでに対応する`Molder`が生成されている場合はキャッシュから返し，
   * そうでなければ`MolderFactory`で生成した後に返されます
   * @param mold 対象となる頂点
   * @return 渡された頂点を`Mold`として`Molding`を行う`Molder`
   */
  def getMolder(mold: Vertex): Molder = {
    molderMapping.getOrElseUpdate(mold, factory.get(mold, this))
  }

  def getHeader(mold: Vertex): VertexHeader = factory.getHeader(mold, this)

  /**
   * 渡されたパターン頂点とパターンマッチしているデータ頂点を返します．
   * 存在しない場合は例外を送出します
   */
  def matchedVertexExact(patternVertex: Vertex): Vertex = {
    binding.get(patternVertex) match {
      case Some(v) => v match {
        case v: Vertex => v
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternVertex not found in $binding}.")
    }
  }

  /**
   * 渡されたパターン接続とパターンマッチしているデータ接続を返します．
   * 存在しない場合は例外を送出します
   */
  def matchedEdgeExact(patternEdge: Edge): Edge = {
    binding.get(patternEdge) match {
      case Some(v) => v match {
        case e: Edge => e
        case _ => throw new IllegalStateException("Invalid matching detected.")
      }
      case None => throw new IllegalStateException(s"Invalid matching: $patternEdge not found in $binding}.")
    }
  }

  /**
   * 渡されたパターン頂点とパターンマッチしているデータ頂点が存在すれば`Option`で返します
   * @param patternVertex チェックするパターン頂点
   * @return パターンマッチしていた場合は，`Some(対応するデータ頂点)`，していなければ`None`
   */
  def matchedVertex(patternVertex: Vertex): Option[Vertex] = {
    binding.get(patternVertex) match {
      case Some(v) => Some(v.asInstanceOf[Vertex])
      case None => None
    }
  }

  /**
   * 渡されたパターン接続とパターンマッチしているデータ接続が存在すれば`Option`で返します
   * @param patternVertex チェックするパターン接続
   * @return パターンマッチしていた場合は，`Some(対応するデータ接続)`，していなければ`None`
   */
  def matchedEdge(patternEdge: Edge): Option[Edge] = {
    binding.get(patternEdge) match {
      case Some(e) => Some(e.asInstanceOf[Edge])
      case None => None
    }
  }

  def unmatchedEdges(patternVertex: Vertex): Iterable[Edge] = {
    binding.unmatchedEdges(matchedVertexExact(patternVertex))
  }
}
