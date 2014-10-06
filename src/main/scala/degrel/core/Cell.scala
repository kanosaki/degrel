package degrel.core

trait Cell extends Vertex {
}

object Cell {
  def apply(docstring: String = "") = {
    create(docstring) { c => Seq()}
  }

  def create(docstring: String = "",
             imports: Iterable[Cell] = Seq())
            (ehFactory: VertexHeader => Iterable[Vertex]): Cell = {
    val header = new CellHeader(null)
    val vertices = ehFactory(header)
    header.write(new CellBody(vertices))
    header
  }

}

