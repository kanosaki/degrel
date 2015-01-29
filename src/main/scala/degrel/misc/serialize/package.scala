package degrel.misc

import degrel.core.Graph

package object serialize {
  type DNodeID = Long

  def toDoc(g: Graph, flavor: FormatFlavor = FormatFlavor.Flat): DDocument = {
    flavor match {
      case FormatFlavor.Flat => new FlatDocument(g)
    }
  }
}
