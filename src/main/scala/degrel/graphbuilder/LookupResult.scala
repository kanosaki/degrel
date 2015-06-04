package degrel.graphbuilder

trait LookupResult[T] {

}

trait LookupFound[T] extends LookupResult[T] {
  def primary: T
}

trait LookupFoundMulti[T] extends LookupFound[T] {
  /**
   * 見つかった要素．先頭の要素ほど優先順位が高く，
   * `items.head == primary`です
   * @return
   */
  def items: Iterable[T]

  override final def primary: T = items.head
}

trait LookupFoundUnique[T] extends LookupFound[T] {

}

trait LookupNotFound[T] extends LookupResult[T]

object LookupResult {

  /**
   * 該当する要素が見つからなかった
   */
  case class NotFound[T]() extends LookupNotFound[T]

  /**
   * 唯一の要素が見つかった
   */
  case class UniqueAny[T](res: T) extends LookupFoundUnique[T] {
    override def primary: T = res
  }

  /**
   * 唯一の要素が，トップレベルにおいて見つかった
   */
  case class UniqueTopLevel[T](res: T) extends LookupFoundUnique[T] {
    override def primary: T = res
  }

  /**
   * それぞれのレベルにおいてユニークな要素が見つかった
   */
  case class UniqueInLevel[T](res: List[Option[T]]) extends LookupFoundMulti[T] {
    override lazy val items: Iterable[T] = res.collect {
      case Some(v) => v
    }
  }

  /**
   * 複数の要素が見つかった．かつ優先順位が定義できている
   */
  case class FoundOrdered[T](res: List[T]) extends LookupFoundMulti[T] {
    override def items: Iterable[T] = res
  }
}
