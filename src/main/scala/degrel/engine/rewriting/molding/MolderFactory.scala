package degrel.engine.rewriting.molding

import degrel.core._

trait MolderFactory {
  def get(mold: Vertex, ctx: MoldingContext): Molder = {
    mold.label match {
      case Label.V.reference => new ReferenceVertexMolder(mold, ctx)
      case _ => new PlainMolder(mold, ctx)
    }
  }

  def getHeader(mold: Vertex, ctx: MoldingContext): VertexHeader = {
    mold.label match {
      case Label.V.cell => new CellHeader(null)
      case _ => new LocalVertexHeader(null)
    }
  }
}

class RootMolderFactory extends MolderFactory {
}

object MolderFactory {
  val default = new RootMolderFactory()
}

