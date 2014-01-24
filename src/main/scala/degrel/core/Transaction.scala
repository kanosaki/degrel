package degrel.core

import scala.concurrent.stm

class Transaction() {
  protected val _status: stm.Ref[TransactionStatus] = stm.Ref(TransacrionStatus.Active)

  def status: TransactionStatus = _status.single.get

  def statusR: stm.Ref[TransactionStatus] = _status

  def complete(): Boolean = {
    _status.single.trySet(TransacrionStatus.Commited)
  }
}

object Transaction {

  class Bottom extends Transaction {
    override protected val _status: stm.Ref[TransactionStatus] = stm.Ref(TransacrionStatus.Commited)
  }


  def bot: Transaction = new Bottom()

}
