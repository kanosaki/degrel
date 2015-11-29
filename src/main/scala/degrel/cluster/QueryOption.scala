package degrel.cluster

trait QueryOption {

}

object QueryOption {

  case class DepthHint(depth: Int) extends QueryOption

  case object WholeCell extends QueryOption

  case object None extends QueryOption

}

