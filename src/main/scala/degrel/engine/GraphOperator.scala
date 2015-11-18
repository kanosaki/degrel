package degrel.engine

import degrel.core.Vertex

import scala.concurrent.{ExecutionContext, Future}

// interface class for graph operation
trait GraphOperator {
  implicit def context: ExecutionContext

  def write(target: Vertex, value: Vertex): Future[Boolean]

  def add(targetCell: Vertex, value: Vertex): Future[Boolean]

  def remove(targetCell: Vertex, value: Vertex): Future[Boolean]
}


class LocalGraphOperator(driver: LocalDriver)(implicit override val context: ExecutionContext) extends GraphOperator {
  override def remove(targetCell: Vertex, value: Vertex): Future[Boolean] = {
    if (targetCell.isCell) {
      val c = targetCell.asCell
      c.removeRoot(value)
      Future(true)
    } else {
      Future(false)
    }
  }

  override def write(target: Vertex, value: Vertex): Future[Boolean] = {
    if (target.isHeader && target.id.hasSameOwner(driver.header.id)) {
      val h = target.asHeader
      h.write(value)
      Future(true)
    } else {
      Future(false)
    }
  }

  override def add(targetCell: Vertex, value: Vertex): Future[Boolean] = {
    if (targetCell.isCell) {
      val c = targetCell.asCell
      c.addRoot(value)
      Future(true)
    } else {
      Future(false)
    }
  }
}
