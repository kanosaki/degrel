package degrel.misc

trait PrettyPrintable {
  def repr: String

  def reprRecursive: String

  def prettyPrint(opt: PrettyPrintOption) = ???
}

trait PrettyPrintOption {

}
