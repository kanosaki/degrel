package degrel.cluster

import java.security.SecureRandom


case class NodeInfo(nodeID: Int)

object NodeInfo {
  def generate(): NodeInfo = {
    NodeInfo(
      SecureRandom.getInstanceStrong.nextInt()
    )
  }
}
