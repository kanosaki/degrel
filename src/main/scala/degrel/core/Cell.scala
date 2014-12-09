package degrel.core

trait Cell extends Vertex {
  def imports: Seq[Cell] = ???
}

object Cell {
  def apply(docstring: String = "") = {
    ???
  }
}

