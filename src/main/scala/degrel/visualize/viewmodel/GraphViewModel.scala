package degrel.visualize.viewmodel

import java.util.concurrent.{TimeUnit, ScheduledThreadPoolExecutor, ScheduledExecutorService}

import degrel.core.{Traverser, Vertex, Element}
import degrel.visualize.viewmodel.grapharranger.{VertexViewModel, DynamicsGraphArranger, GraphArranger}
import degrel.utils.Signal

import scala.collection.JavaConversions

class GraphViewModel(var depth: Int, tickIntervalMillisec: Long = 10) {
  private var arranger: GraphArranger = new DynamicsGraphArranger()
  private val modelUpdater: ScheduledExecutorService = new ScheduledThreadPoolExecutor(1)

  val onRequestRedraw = Signal[Unit]()

  def setElement(elem: Element) = {
    arranger.clear()
    elem match {
      case v: Vertex => Traverser(v, depth).foreach(arranger.pushVertex)
    }
    arranger.stickVertex(elem.asInstanceOf[Vertex])
    modelUpdater.scheduleWithFixedDelay(new Runnable {
      override def run(): Unit = {
        tick()
        onRequestRedraw.trigger(this, ())
      }
    }, 50, tickIntervalMillisec, TimeUnit.MILLISECONDS)
  }

  def graphArranger = arranger

  def graphArranger_=(arr: GraphArranger) = { arranger = arr }

  def vertices: Iterable[VertexViewModel] = {
    arranger.vertices
  }

  def tick(): Unit = {
    arranger.tick()
    if (arranger.isCompleted) {
      modelUpdater.shutdown()
    }
  }

  def verticesJ = JavaConversions.asJavaIterable(arranger.vertices)

  def edgesJ = JavaConversions.asJavaIterable(arranger.edges)

  def dispose(): Unit = {
    modelUpdater.shutdown()
  }
}
