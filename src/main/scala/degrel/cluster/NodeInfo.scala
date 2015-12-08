package degrel.cluster

import java.security.SecureRandom

import degrel.core.NodeID


case class NodeInfo(nodeID: NodeID)

object NodeInfo {
  def generate(): NodeInfo = {
    NodeInfo(
      SecureRandom.getInstanceStrong.nextInt()
    )
  }
}
