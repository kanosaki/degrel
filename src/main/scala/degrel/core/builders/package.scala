package degrel.core

package object builders {
  def freeze(v: Vertex): Vertex = Freezer(v)

  def duplicate(v: Vertex): Vertex = Duplicator(v)
}
