package degrel.engine.rewriting.molding

import degrel.core.{Label, Vertex}

trait MolderFactory {
  def get(mold: Vertex, ctx: MoldingContext): Molder = {
    mold.label match {
      case Label.V.reference => new ReferenceVertexMolder(mold, ctx)
      case _ => new PlainMolder(mold, ctx)
    }
  }
}

class RootMolderFactory extends MolderFactory {
}

object MolderFactory {
  val default = new RootMolderFactory()
}

