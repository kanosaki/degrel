package degrel.engine

import java.util.concurrent.{ConcurrentLinkedDeque, TimeUnit, LinkedBlockingQueue}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import degrel.utils.{ResourceGuard, ReadWriteGuard, ConcurrentHashSet}
import degrel.rewriting.Reserve
import akka.actor.{Props, Actor, ActorLogging, ActorRef}
import scala.collection.JavaConversions._

object RewriteScheduler {

  case object Run

  case object Completed

  def props(reserve: Reserve): Props = Props(classOf[RewriteScheduler], reserve)

  def apply(reserve: Reserve): ActorRef = {
    degrel.engine.system.actorOf(this.props(reserve))
  }
}

/**
 * 並行書き換えを書くRewriterが停止するまで実行します．
 * 一度にすべての書き換えが行われないよう，書き換えは1ステップずつ行われます．
 * @param reserve
 */
class RewriteScheduler(val reserve: Reserve) extends Actor with ActorLogging {

  def receive: Actor.Receive = {
    case RewriteScheduler.Run => {
      this.run()
      sender ! RewriteScheduler.Completed
    }
  }

  /**
   * まだ書き換えを行っていない，または停止していないRewriter(へのActorRef)です
   * また，どこかで書き換えが実行された場合はまた書き換えを行う必要がある可能性があるので，
   * `stopped`から停止したRewriterが復帰します
   */
  private val queued = new LinkedBlockingQueue[ActorRef]()

  /**
   * 現在書き換えを行っているRewriter．RewriterにStepメッセージを送ってから
   * 結果を受信するまでの間はここにプールされます
   */
  private val working = new ConcurrentHashSet[ActorRef]()

  /**
   * 書き換えが停止したRewriter．ほかのRewriterが変更を加えた場合
   * そこが書き換え可能になる可能性があるので，`queued`へ復帰します．
   */
  private val stopped = new ConcurrentLinkedDeque[ActorRef]()

  /**
   * 各キューの状態を読み取り・変更するときに取得する`ResourceGuard`
   */
  private val modifing = new ResourceGuard()

  reserve.rewriters.foreach(e => queued.put(RewriterWorker(e)))

  private def requeueWorkers() = {
    for (st <- stopped) {
      queued.put(st)
    }
    stopped.clear()
  }

  def run() = {
    implicit val timeout = Timeout(5.seconds)
    import system.dispatcher
    do {
      val next = queued.poll(100, TimeUnit.MILLISECONDS)
      if (next != null) {
        working += next
        val future = next ? RewriterWorker.Step(reserve)
        future.onSuccess {
          case RewriterWorker.Result(true) => {
            modifing.lock {
              queued.put(next)
              this.requeueWorkers()
              working -= next
            }
          }
          case RewriterWorker.Result(false) => {
            modifing.lock {
              working -= next
              stopped.add(next)
            }
          }
        }
      }
    } while (!this.isStopped)
  }

  def isStopped: Boolean = modifing.lock {queued.isEmpty && working.isEmpty}

}
