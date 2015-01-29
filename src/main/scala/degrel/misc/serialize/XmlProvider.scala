package degrel.misc.serialize

import scala.xml.Elem

class XmlProvider extends FormatProvider[DDocument, Elem] {
  override def dump(in: DDocument): Elem = {
    val vs = in.vertices.map(toXElem)
    <graph>
      {vs}
    </graph>
  }

  protected def toXElem(v: DVertex): Elem = {
    <vertex id={v.id.toString} label={v.label}>
      {v.edges.map {
      case DEdge(lbl, DRef(id)) => <edge label={lbl} ref={id.toString}/>
      case DEdge(lbl, dv: DVertex) =>
        <edge label={lbl}>
          {toXElem(dv)}
        </edge>
    }}
    </vertex>
  }

  override def load(in: Elem): DDocument = ???
}
