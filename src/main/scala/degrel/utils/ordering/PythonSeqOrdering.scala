package degrel.utils.ordering

class PythonSeqOrdering[T <% Ordered[T]] extends Ordering[Seq[T]] {
  override def compare(xs: Seq[T], ys: Seq[T]): Int = {
    val pairs: Seq[(T, T)] = xs.zip(ys)
    if (pairs.find(xy => xy._1.compareTo(xy._2) == 1).nonEmpty) {
      1
    } else if (pairs.find(xy => xy._1.compareTo(xy._2) == -1).nonEmpty) {
      -1
    } else {
      xs.size.compareTo(ys.size)
    }
  }
}

