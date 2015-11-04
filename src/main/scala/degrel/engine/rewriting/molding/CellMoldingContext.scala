package degrel.engine.rewriting.molding

import degrel.engine.Driver
import degrel.engine.rewriting.Binding

class CellMoldingContext(val cellMolder: CellMolder,
                         binding: Binding,
                         molderFactory: MolderFactory,
                         driver: Driver) extends MoldingContextBase(binding, molderFactory, driver) {
  override def ownerMolder: Molder = cellMolder
}

object CellMoldingContext {
  def apply(cellMolder: CellMolder, baseContext: MoldingContext): CellMoldingContext = {
    new CellMoldingContext(cellMolder, baseContext.binding, baseContext.factory, baseContext.driver)
  }
}
