package degrel.utils

import degrel.core.SwitchingFreezable
import degrel.front.FrontException

import scala.collection.mutable

/**
 * 名前解決エラー．指定された変数名等が見つからない場合にthrowされる．
 */
class NameError(expr: Any, msg: String)
  extends FrontException(s"NameError: ${expr.toString} - $msg") {
}

/**
 * 木構造を持つMultiMapです．
 * 木のあるノードでの検索結果と，親のノードでの探索結果をすべて結合した結果がリストで返されます．
 * その際に末端での結果ほどリストの前方に出現し，木の根の方での結果ほどリストの末尾に現れます
 */
trait TreeMap[TKey, TValue] extends SwitchingFreezable {
  protected val parent: TreeMap[TKey, TValue]
  /**
   * キーと値のテーブル．MultiMapを使用します
   */
  protected val symbolMap: mutable.MultiMap[TKey, TValue] =
    new mutable.HashMap[TKey, mutable.Set[TValue]]
      with mutable.MultiMap[TKey, TValue]

  /**
   * このノードに値を登録します
   * @param key 登録するキー
   * @param bindTarget 登録する値
   * @return
   */
  def bindSymbol(key: TKey, bindTarget: TValue): Unit = {
    if (this.isFrozen) {
      throw new RuntimeException("Cannot add symbol, already frozen.")
    }
    symbolMap.addBinding(key, bindTarget)
  }

  /**
   * 探索木を再帰的に探索し，見つかった物すべてを結合して一つのリストにして返します
   */
  def resolve(expr: TKey): List[TValue] = {
    this.resolveGrouped(expr).flatten
  }

  /**
   * 探索木を再帰的に探索し，各ノードからの結果のリストを結合してリストのリストとして結果を返します
   * @param key 探索するキー
   * @return 探索ノードごとにリストを作ったリスト
   */
  def resolveGrouped(key: TKey): List[List[TValue]] = {
    if (parent != null) {
      resolveInThis(key) :: parent.resolveGrouped(key)
    } else {
      List(resolveInThis(key))
    }
  }

  /**
   * 木を再帰的に探索し，ただ一つのみ見つかった場合はそれを返します
   * @param key 探索するキー
   * @throws NameError 条件に合わず，かつデフォルト値が与えられていない場合は例外を送出します
   */
  def resolveExact(key: TKey, default: Option[TValue] = None): TValue = {
    this.resolve(key) match {
      case Nil => throwOrDefault(default, new NameError(key, "No mapping found"))
      case value :: Nil => value
      case _ => throwOrDefault(default, new NameError(key, "Duplicated mapping found"))
    }
  }

  /**
   * デフォルト値がSomeの場合はデフォルト値を返し，そうで無ければ例外を送出します
   * @param default デフォルトの値
   * @param error デフォルトの値がNoneの時に送出される例外．名前渡しです
   */
  protected def throwOrDefault(default: Option[TValue], error: => Exception): TValue = {
    default match {
      case Some(defval) => defval
      case None => throw error
    }
  }

  protected def resolveInThis(expr: TKey): List[TValue] = {
    symbolMap.get(expr) match {
      case Some(vs) => vs.toList
      case None => List()
    }
  }

  override def freeze: this.type = {
    if (this.parent != null) this.parent.freeze
    super.freeze
    this
  }

  def repr(level: Int): String = {
    val thisLevel = s"$level: ${this.symbolMap.toString()}"
    if(this.parent != null) {
      thisLevel + "\n" + this.parent.repr(level + 1)
    } else {
      thisLevel
    }
  }

  override def toString: String = {
    s"TreeMap-----\n${this.repr(0)}\n------------"
  }
}

object TreeMap {
  def empty[K, V]() = {
    val root = new TreeMapRoot[K, V]()
    new TreeMapNode[K, V](root)
  }

  def child[K, V](parent: TreeMap[K, V]) = {
    new TreeMapNode(parent)
  }
}

class TreeMapNode[TK, TV](val parent: TreeMap[TK, TV]) extends TreeMap[TK, TV] {

}

class TreeMapRoot[TK, TV] extends TreeMap[TK, TV] {
  override protected val parent: TreeMap[TK, TV] = null

  /**
   * @inheritdoc
   */
  override def resolveGrouped(key: TK): List[List[TV]] = Nil
}
