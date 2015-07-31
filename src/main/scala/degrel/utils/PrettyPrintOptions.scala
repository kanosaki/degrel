package degrel.utils

case class PrettyPrintOptions(showAllId: Boolean = false,
                              multiLine: Boolean = false,
                              indentItem: String = "  ",
                              showType: Boolean = false,
                              showCellEdgeLabel: Boolean = false) {

}

object PrettyPrintOptions {
  final val default = PrettyPrintOptions()
  final val verbose = PrettyPrintOptions(showAllId = true,
                                         multiLine = true)
  final val debug = PrettyPrintOptions(showAllId = true,
                                       multiLine = true,
                                       showType = true,
                                       showCellEdgeLabel = true)
}

