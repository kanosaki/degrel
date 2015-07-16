package degrel.engine.rewriting.molding

import degrel.core._

trait MolderFactory {
  def get(mold: Vertex, ctx: MoldingContext): Molder = {
    if (mold.isValue) {
      return new ValueMolder(mold, ctx)
    }
    mold.label match {
      case Label.V.reference => {
        val refTarget = mold.thruSingle(Label.E.ref)
        if (ctx.matchedVertex(refTarget).isDefined) {
          new ReferenceVertexMolder(mold, ctx)
        } else {
          new PlainMolder(mold, ctx)
        }
      }
      case Label.V.cell => new CellMolder(mold, ctx)
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

