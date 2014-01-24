package degrel.core

import scala.concurrent.stm

class VertexLocator(_newVertex: VertexBody, _oldVertex: VertexBody)(implicit transaction: Transaction) {

  protected val _oldV: stm.Ref[VertexBody] = stm.Ref(_oldVertex)
  protected val _newV: stm.Ref[VertexBody] = stm.Ref(_newVertex)

  def status: TransactionStatus = transaction.status

  def tryCommit(v: Vertex): Boolean = {
    val vb = v match {
      case vh: VertexHeader => vh.body
      case _vb: VertexBody => _vb
    }
    stm.atomic {
      implicit txn =>
        if (transaction.statusR.get == TransacrionStatus.Active) {
          _newV.set(vb)
          true
        } else {
          false
        }
    }
  }

  def oldVertex: VertexBody = {
    _oldV.single.get
  }

  def newVertex: VertexBody = {
    _newV.single.get
  }

  def activeVertex: VertexBody = {
    val S = TransacrionStatus
    this.status match {
      case S.Active => this.oldVertex
      case S.Aborted => this.oldVertex
      case S.Commited => this.newVertex
    }
  }

}

object VertexLocator {
  def createNew(src: VertexBody)(implicit txn: Transaction = Transaction.bot) = {
    new VertexLocator(src, null)
  }

  def createFrom(prev: VertexLocator)(implicit txn: Transaction): VertexLocator = {
    require(prev != null)
    new VertexLocator(copyVertex(prev.activeVertex), prev.activeVertex)
  }

  def copyVertex(v: VertexBody): VertexBody = {
    if (v == null) return null
    v.shallowCopy.asInstanceOf[VertexBody]
  }
}
