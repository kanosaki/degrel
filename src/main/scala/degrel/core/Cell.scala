package degrel.core

trait Cell extends Vertex {
  def imports: Seq[Cell] = ???
  def rules: Seq[Rule] = this.thru(Label.V.rule).map(_.asRule).toSeq
}

object Cell {
  def apply(docstring: String = "") = {
    ???
  }
}

