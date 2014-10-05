package degrel.utils

package object collection {
  /**
   * {@code Object.==}に従ってリストをグループ化します
   * <pre>[2, 2, 3, 1, 1, 1] --> [ [2, 2], [3], [1, 1, 1] ]</pre>
   * @param list グループ化するリスト
   * @tparam T リストの要素の型
   * @return グループ化された {@code List[List[T]]}
   * @todo Iterable[T]に一般化しておきたい
   */
  def split[T](list: List[T]): List[List[T]] = list match {
    case Nil => Nil
    case h :: t => val segment = list.takeWhile {
      h.==
    }
      segment :: split(list drop segment.length)
  }
}
