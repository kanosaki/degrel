package degrel.core


case class TraverseRegion(inner: Boolean, wall: Boolean, outer: Boolean) {
}

object TraverseRegion {
  val WallOnly = TraverseRegion(inner = false, wall = true, outer = false)
  val AllArea = TraverseRegion(inner = true, wall = true, outer = true)
  val InnerOnly = TraverseRegion(inner = true, wall = false, outer = false)
  val InnerAndWall = TraverseRegion(inner = true, wall = true, outer = false)
}
