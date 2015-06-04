package degrel.utils

import scala.collection.mutable

object C3 {
  def linearize[T <: HasParents[T]](node: T): List[T] = new C3().linearize(node)

  def merge[T <: HasParents[T]](nodes: List[List[T]]) = new C3().merge(nodes)
}

/**
 * C3 Linearization algorithm implementation
 * http://en.wikipedia.org/wiki/C3_linearization
 */
class C3[T <: HasParents[T]] {

  /**
   * C3線形化アルゴリズムを用いて，継承グラフを線形化します．
   * ただし，巡回がある場合は例外が投げられます
   * @param node 起点となる子孫要素
   * @return 線形化した継承グラフ
   */
  def linearize(node: T): List[T] = {
    if (hasCycle(node, Set())) {
      throw new IllegalArgumentException("Cyclic graph is not allowed.")
    }
    node :: merge(node.parents.map(linearize) ++ List(node.parents))
  }

  def merge(nodes: List[List[T]]): List[T] = {
    if (nodes.isEmpty || nodes.head.isEmpty) return Nil
    merge1(List(), nodes)
  }

  /**
   * 与えられたMultiParentノードを根とするグラフは巡回構造があるかどうかを検査します
   */
  def hasCycle(node: T, history: Set[T]): Boolean = {
    if (history.contains(node)) {
      true
    } else {
      val parents = node.parents
      if (parents.isEmpty) {
        false
      } else {
        val nextHistory = history + node
        ! parents.forall(!hasCycle(_, nextHistory))
      }
    }
  }

  private def merge1(checked: List[List[T]],
                     unchecked: List[List[T]]): List[T] = {
    if (unchecked.isEmpty) {
      if (checked.nonEmpty) {
        return merge1(List(), checked.reverse)
      } else {
        return Nil
      }
    }
    val pivot = unchecked.head
    val candidate = pivot.head
    val candidateIsAcceptable =
      checked.forall(_.tail.forall(_ != candidate)) &&
        unchecked.tail.forall(_.tail.forall(_ != candidate))
    val nextChecked = (pivot.tail :: checked).filter(_ != Nil)
    if (candidateIsAcceptable) {
      val nextUnchecked = unchecked.tail.map {
        case first :: tail if first == candidate => tail
        case other => other
      }.filter(_ != Nil)
      candidate :: merge1(nextChecked, nextUnchecked)
    } else {
      merge1(nextChecked, unchecked.tail)
    }
  }
}

class MemoizedC3[T <: HasParents[T]](parentC3: Iterable[MemoizedC3[T]])
  extends C3[T] {

  private[this] val linearizeCache = new mutable.HashMap[T, List[T]]()
  private[this] val hasCycleCache = new mutable.HashMap[T, Boolean]()

  def isLinerizeCached(target: T) = linearizeCache.contains(target)

  def isHasCycleCached(target: T) = hasCycleCache.contains(target)

  /**
   * @inheritdoc
   */
  override def linearize(node: T): List[T] = {
    linearizeCache.getOrElseUpdate(node, super.linearize(node))
  }

  /**
   * 与えられたMultiParentノードを根とするグラフは巡回構造があるかどうかを検査します
   */
  override def hasCycle(node: T, history: Set[T]): Boolean = {
    hasCycleCache.getOrElseUpdate(node, super.hasCycle(node, history))
  }
}

trait HasParents[T] {
  def parents: List[T]
}

