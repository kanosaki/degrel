package degrel.utils

/**
 * C3 Linearization algorithm implementation
 * http://en.wikipedia.org/wiki/C3_linearization
 */
object C3 {

  /**
   * C3線形化アルゴリズムを用いて，継承グラフを線形化します．
   * ただし，巡回がある場合は例外が投げられます
   * @param node 起点となる子孫要素
   * @return 線形化した継承グラフ
   */
  def linearize[T <: MultiParent](node: T): List[T] = {
    if (hasCycle(node, Set())) {
      throw new IllegalArgumentException("Cyclic graph is not allowed.")
    }
    (node :: merge(node.parents.map(linearize) ++ List(node.parents))).asInstanceOf[List[T]]
  }

  def merge(nodes: List[List[MultiParent]]): List[MultiParent] = {
    if (nodes.isEmpty || nodes.head.isEmpty) return Nil
    merge1(List(), nodes)
  }

  /**
   * 与えられたMultiParentノードを根とするグラフは巡回構造があるかどうかを検査します
   */
  def hasCycle(node: MultiParent, history: Set[MultiParent]): Boolean = {
    if (history.contains(node)) {
      true
    } else {
      if (node.parents.isEmpty) {
        false
      } else {
        val nextHistory = history + node
        ! node.parents.forall(!hasCycle(_, nextHistory))
      }
    }
  }

  private def merge1(checked: List[List[MultiParent]],
                     unchecked: List[List[MultiParent]]): List[MultiParent] = {
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

trait MultiParent {

  def parents: List[MultiParent]
}
