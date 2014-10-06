package degrel.utils

package object collection {

  implicit def defaultEqualityComparator[T]: (T, T) => Boolean = _ == _

  /**
   * {@code Object.==}に従ってリストをグループ化します
   * <pre>[2, 2, 3, 1, 1, 1] --> [ [2, 2], [3], [1, 1, 1] ]</pre>
   * @param list グループ化するリスト
   * @tparam T リストの要素の型
   * @return グループ化された {@code List[List[T]]}
   * @todo Iterable[T]に一般化しておきたい
   */
  def split[T](source: Iterable[T])(implicit comp: (T, T) => Boolean = null): Seq[Seq[T]] = {
    new SplittingIterator[T](source.iterator)(comp).map(_.toSeq).toSeq
  }
}
