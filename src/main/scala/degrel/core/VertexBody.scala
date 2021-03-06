package degrel.core

import java.lang.ref.WeakReference


object VertexBody {
  def apply(label: Label, attributes: Map[Label, String], allEdges: Iterable[Edge], id: ID): VertexBody = {
    label match {
      case Label.V.reference => new ReferenceVertexBody(label, attributes, allEdges)
      case Label.V.cell => CellBody(allEdges)
      case _ => new LocalVertexBody(label, attributes, allEdges)
    }
  }
}

trait VertexBody extends Vertex {
  protected var headerRef: WeakReference[VertexHeader] = null

  var fingerprintCache: Long = Fingerprint.EMPTY


  def attr(key: Label): Option[String] = {
    attributes.get(key)
  }

  def reprLabel: String = {
    this.attributes.get("__captured_as__") match {
      case Some(capExpr) => s"$capExpr[${this.label.expr}]"
      case None => s"${this.label.expr}"
    }
  }

  def reprAttrs: String = {
    val targetKvs = attributes
      .filter { case (k, v) => !k.isMeta }
    if (targetKvs.nonEmpty) {
      val kvsExpr = targetKvs.map { case (k, v) => s"$k:$v" }.mkString(", ")
      s"{$kvsExpr}"
    } else {
      ""
    }
  }

  override def asCell: Cell = {
    this.asCellBody
  }

  def asCellBody: CellBody = {
    require(this.label == Label.V.cell)
    CellBody(this.edges)
  }

  override def isBody = true

  def header: VertexHeader = {
    if (this.headerRef == null) {
      null
    } else {
      this.headerRef.get()
    }
  }

  def header_=(vh: VertexHeader) = {
    this.headerRef = new WeakReference[VertexHeader](vh)
  }

  override val id: ID = ID.autoAssign
}

