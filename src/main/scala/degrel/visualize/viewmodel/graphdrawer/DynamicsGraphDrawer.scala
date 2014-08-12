package degrel.visualize.viewmodel.graphdrawer

import degrel.core.Vertex

import scala.collection.mutable

class DynamicsGraphDrawer(var stableThreshould: Float = 10f,
                          var depth: Int = 10,
                          var withGravity: Boolean = false) extends GraphDrawer {
  val vertices = mutable.LinearSeq[VertexAdapter]()
  /**
   * すべてのノード間のクーロン定数
   */
  var coulombConstant: Float = 1
  /**
   * 接続されているノード間のバネ係数
   */
  var springConstant: Float = 1
  /**
   * ノード間のバネの自然長
   */
  var neutralLength: Float = 10
  /**
   * 減衰
   */
  var attenuation = 0.8

  /**
   * 微少時間
   */
  var timeDelta = 0.1

  def tick() = {
    for (v <- vertices) {
      v.update()
    }
  }

  def grossEnergy = {
    vertices.foldLeft(0d)((p, vs) => {p + vs.velocity.norm * vs.weight})
  }


  class VertexAdapter(val origin: Vertex) {
    var isFixed = false
    var radius: Float = 0
    var weight = 1
    var location = Vec.zero
    var force = Vec.zero
    var velocity = Vec.zero

    def update() = {
      val currentForce = Vec.zero
      val neighborSet = origin.edges().map(_.dst).toSet

      for (v <- vertices) {
        val delta = v.location - this.location
        // implicitな値を渡さないとコンパイル通らない
        val deltaNorm: Double = delta.norm
        // 空間に存在するすべての頂点からクーロン力を受けます
        currentForce += delta * (coulombConstant / math.pow(deltaNorm, 2))
        if (neighborSet.contains(v.origin)) {
          // 接続を持つ場合は，バネのちから受けます
          currentForce += delta * (springConstant * (deltaNorm - neutralLength))
        }

        // Update states
        velocity = (velocity + currentForce * timeDelta / weight) * attenuation
        location += velocity * timeDelta
      }
    }
  }

}
