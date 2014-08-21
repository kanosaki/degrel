package degrel.visualize.viewmodel.grapharranger

import degrel.core.{ID, Edge, Vertex}
import degrel.utils.collection.mutable.RingBuffer
import degrel.visualize.Vec

import scala.collection.mutable

class DynamicsGraphArranger(initialVertices: Iterable[Vertex] = Seq(),
                            var stableThreshould: Float = 1f,
                            var withGravity: Boolean = false) extends GraphArranger {
  initialVertices.foreach(pushVertex)

  val edges = new mutable.ListBuffer[EdgeAdapter]()

  val adapterMapping = new mutable.HashMap[ID, VertexAdapter]()

  val stickedVertices = new mutable.HashSet[ID]()

  def vertices: Iterable[VertexAdapter] = adapterMapping.values

  override def pushVertex(v: Vertex) = {
    adapterMapping += v.id -> new VertexAdapter(v)
    v.edges().foreach(edges += new EdgeAdapter(_))
  }

  override def stickVertex(v: Vertex) = {
    stickedVertices += v.id
  }

  /**
   * すべてのノード間のクーロン定数
   */
  var coulombConstant: Float = 1000

  /**
   * 接続されているノード間のバネ係数
   */
  var springConstant: Float = -10

  /**
   * ノード間のバネの自然長
   */
  var neutralLength: Float = 100

  /**
   * 減衰
   */
  var attenuation = 0.5

  /**
   * 微少時間(単位なし)
   */
  var sliceTime = 0.2

  var historySize = 10
  var history = new RingBuffer[Double](historySize)

  /**
   * 変動係数を指定し，エネルギーの変動がこれを下回れば安定とします
   */
  var stableThresh = 0.005

  var gravity = new Vec(0, 0.5)

  def cofficientVariationOfEnergy = {
    val energySum = history.sum
    val energyAve = energySum / history.size
    val variance = history.foldLeft(0d)((p, v) => p + math.pow(v - energyAve, 2))
    // 変動係数
    math.sqrt(variance) / energyAve
  }

  def isStable: Boolean = {
    history.size == history.maxSize && this.cofficientVariationOfEnergy < stableThresh
  }

  def isCompleted = isStable

  def tick() = {
    for (v <- vertices.filterNot(p => stickedVertices(p.origin.id))) {
      v.step()
    }
    history += this.grossEnergy
    println(grossEnergy)
  }

  def grossEnergy = {
    vertices.foldLeft(0d)((p, vs) => {p + vs.velocity.norm * vs.weight})
  }


  class VertexAdapter(val origin: Vertex) extends VertexViewModel {
    var isFixed = false
    var weight = 1
    var location = Vec.random() * neutralLength * 3
    var force = Vec.zero
    var velocity = Vec.zero

    def step() = {
      val currentForce = Vec.zero
      val neighborSet = origin.edges().map(_.dst).toSet

      for (v <- vertices.filter(_ ne this)) {
        var delta = this.location - v.location
        var deltaNormSq: Double = delta.normSq
        while (deltaNormSq < Double.MinPositiveValue) {
          delta = Vec.random()
          deltaNormSq = delta.norm
        }
        val deltaNorm = math.sqrt(deltaNormSq)
        val normalizedDelta = delta / deltaNorm
        // 空間に存在するすべての頂点からクーロン力を受けます
        currentForce += normalizedDelta * (coulombConstant / deltaNormSq)
        if (neighborSet.contains(v.origin)) {
          // 接続を持つ場合は，バネのちから受けます
          currentForce += normalizedDelta * (springConstant * (deltaNorm - neutralLength))
        }
        if (withGravity) {
          currentForce += gravity * weight
        }

        // Update states
        velocity = (velocity + currentForce * sliceTime / weight) * attenuation
        location += velocity * sliceTime
      }
    }
  }

  class EdgeAdapter(val origin: Edge) extends EdgeViewModel {
    override def from: Vec = {
      adapterMapping(origin.src.id).location
    }

    override def to: Vec = {
      adapterMapping(origin.dst.id).location
    }
  }

  override def clear(): Unit = {
    adapterMapping.clear()
    stickedVertices.clear()
    edges.clear()
  }
}
