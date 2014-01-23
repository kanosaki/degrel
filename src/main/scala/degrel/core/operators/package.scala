package degrel.core

package object operators {
  def freeze(v: Vertex): Vertex = Freezer(v)

  def duplicate(v: Vertex): Vertex = Duplicator(v)

  def areSame(one: Vertex, another: Vertex) = {
    val comparator = new EqualityComparator(one, another)
    comparator.isSame
  }
}
