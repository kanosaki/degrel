package degrel.utils

import scala.collection.mutable

class ChainMap[K, V](val parents: List[ChainMap[K, V]]) {

  protected val map = new mutable.HashMap[K, mutable.Set[V]] with mutable.MultiMap[K, V]

}

object ChainMap {
  def apply[K, V](parents: ChainMap[K, V]*) = new ChainMap[K, V](parents.toList)
}
