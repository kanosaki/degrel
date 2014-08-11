package degrel.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class SignalTest extends FlatSpec with MockFactory {
  it should "call handler with arguments" in {
    val sig = Signal[Int]()
    val handler = mockFunction[Any, Int, Unit]
    sig.register(handler)
    inSequence(
    {
      handler.expects(null, 1).once()
      handler.expects(null, 2).once()
      handler.expects(null, 3).once()
    })
    sig.trigger(null, 1)
    sig.trigger(null, 2)
    sig.trigger(null, 3)
  }

  it should "handle signal in concurrent" in {
    val sig = new signal.ThreadPoolSignal[String](1000)
    val h1 = mockFunction[Any, String, Unit]
    val h2 = mockFunction[Any, String, Unit]
    val h3 = mockFunction[Any, String, Unit]
    sig.register(h1)
    sig.register(h2)
    sig.register(h3)

    val triggerThread = Thread.currentThread()

    inSequence(
    {
      h1.expects(null, "FOOBAR").once()
        .onCall((sender, arg) =>
                  assert(Thread.currentThread() != triggerThread)
               )
      h1.expects(null, "HOGEHOGE").once()
    })
    inSequence(
    {
      h2.expects(null, "FOOBAR").once()
        .onCall((sender, arg) =>
                  assert(Thread.currentThread() != triggerThread)
               )
      h2.expects(null, "HOGEHOGE").once()
    })
    inSequence(
    {
      h3.expects(null, "FOOBAR").once()
        .onCall((sender, arg) =>
                  assert(Thread.currentThread() != triggerThread)
               )
      h3.expects(null, "HOGEHOGE").once()
    })
    sig.trigger(null, "FOOBAR")
    sig.trigger(null, "HOGEHOGE")
  }
}
