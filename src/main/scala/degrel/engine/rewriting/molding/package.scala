package degrel.engine.rewriting

import degrel.core.Vertex

package object molding {

  def mold(mold: Vertex, binding: Binding)
          (implicit molderFactory: MolderFactory = MolderFactory.default) : Vertex = {
    val context = new MoldingContext(binding, molderFactory)
    val rootMolder = context.getMolder(mold)
    Molder.phases.foreach(rootMolder.process(_))
    rootMolder.header
  }
}
