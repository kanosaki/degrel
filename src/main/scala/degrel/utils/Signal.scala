package degrel.utils

object Signal {
  def apply[T]() = {
    new signal.SequentialSignal[T]()
  }

  trait DispatchMethod

  object DispatchMethod {

    case object Sequential extends DispatchMethod

    case object Concurrent extends DispatchMethod

  }

}
