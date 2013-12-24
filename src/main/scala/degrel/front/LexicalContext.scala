package degrel.front

import scala.collection.mutable
import scala.reflect.ClassTag
import degrel.core

/**
 * 名前解決エラー．指定された変数名等が見つからない場合にthrowされる．
 */
class NameError(expr: String) extends FrontException(s"$expr not found") {
  override def toString = {
    expr
  }
}

/**
 * プログラム上での環境を表現します
 * プログラムファイルではモジュールの環境
 * 書き換えルールの場合は左辺の環境
 * 等を束縛します
 */
trait LexicalContext {
  protected val parent: LexicalContext

  /**
   * 現在，パターン(ルール左辺)であるかどうかを返します
   */
  def isPattern = false

  /**
   * シンボルと値のテーブル
   */
  protected val symbolMap: mutable.MultiMap[String, Any] =
    new mutable.HashMap[String, mutable.Set[Any]] with mutable.MultiMap[String, Any]

  /**
   * シンボルから値のリストを返します
   * @param expr 探索するシンボル
   * @return 見つかった値のリスト，より近いコンテキストものが前に来ます
   */
  def resolve(expr: String): List[Any] = {
    resolveInThis(expr) ++ parent.resolve(expr)
  }

  /**
   * 指定したシンボル，型を持つ値がただ一つ存在する場合はその値を返します
   * @param expr
   * @tparam T
   * @return
   */
  def resolveExact[T: ClassTag](expr: String): T = {
    val klass = implicitly[ClassTag[T]].runtimeClass
    this.resolve(expr) match {
      case value :: Nil if klass.isInstance(value) => value.asInstanceOf[T]
      case _ => throw new NameError(expr)
    }
  }

  protected def resolveInThis(expr: String): List[Any] = {
    symbolMap.get(expr) match {
      case Some(vs) => vs.toList
      case None => List()
    }
  }
}

object LexicalContext {
  def empty: LexicalContext = {
    new RootContext()
  }
}

/**
 * LexcalContextはチェインするので，その起点となるオブジェクト
 */
class RootContext extends LexicalContext {
  val parent: LexicalContext = null

  override def resolve(expr: String): List[Any] = {
    Nil
  }
}

/**
 * ファイルのスコープ
 */
class FileContext(val parent: LexicalContext) extends LexicalContext {

}

/**
 * 規則右辺のコンテキスト．ルール右辺では，ルール左辺での束縛が含まれる
 */
class RhsContext(val parent: LexicalContext)(val lhsContext: LhsContext) extends LexicalContext {
  for ((sym, graph) <- lhsContext.toSymMap)
    symbolMap.addBinding(sym, graph)
}

/**
 * ルール左辺のコンテキスト．
 * ルール左辺では最初に変数の束縛が行われた後にグラフの構築が行われるが，
 * すでに束縛済みの場合は新規にグラフを構成するのではなく，束縛として渡したグラフを渡す必要がある．
 * これは束縛と実際に構成されるグラフのインスタンスに一貫性を持たせるためである
 * @example "A[foo](..)"という頂点が左辺に現れた場合，最初にLhsContextにシンボルと対応する頂点"A" -> foo(...)が登録される
 *          その後，グラフを構成するときに，変数が頂点に存在する場合は，LhsContextからシンボルを取得しそれを使用する必要がある
 * @param parent
 */
class LhsContext(val parent: LexicalContext) extends LexicalContext {
  private val captureCache = new mutable.HashMap[AstVertex, core.Vertex]
  private val lhsCaptureMap = new mutable.HashMap[String, AstVertex]

  override def isPattern = true

  def fromCaptureCache(vertex: AstVertex): Option[core.Vertex] = {
    captureCache.get(vertex)
  }

  def storeCaptureCache(astV: AstVertex, coreV: core.Vertex) {
    captureCache += (astV -> coreV)
  }

  def fromCaptureMap(sym: String) = lhsCaptureMap.get(sym)

  def storeCaptureMap(sym: String, coreV: AstVertex) = lhsCaptureMap.put(sym, coreV)

  def toSymMap = {
    lhsCaptureMap.mapValues(_.toGraph(this))
  }
}
