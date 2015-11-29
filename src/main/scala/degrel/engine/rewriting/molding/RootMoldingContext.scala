package degrel.engine.rewriting.molding

import degrel.core.Vertex
import degrel.engine.Driver
import degrel.engine.rewriting.Binding

class RootMoldingContext(_binding: Binding, _factory: MolderFactory, _driver: Driver) extends MoldingContextBase(_binding, _factory, _driver) {
  override def ownerHeader: Vertex = _driver.header
}
