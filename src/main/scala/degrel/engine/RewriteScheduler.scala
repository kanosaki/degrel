package degrel.engine

import java.util.concurrent.{ConcurrentLinkedDeque, TimeUnit, LinkedBlockingQueue}
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import degrel.utils.{ResourceGuard, ConcurrentHashSet}
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

  // `Reserve`から`Rewriter`を`queued`へ追加します
  reserve.rewriters.foreach(e => queued.put(RewriterWorker(e)))

  private def requeueWorkers() = {
    for (st <- stopped) {
      queued.put(st)
    }
    stopped.clear()
  }

  /**
   * 対象となる`Rewriter`を参照する`ActorRef`を最初に受け取り，その`Rewriter`が`Step`を完了したときの結果によって処理を行う
   * `PartialFunction[Any, Unit]`を返します
   * それぞれの`Rewriter`が完了した際にfuture.onSuccessによって呼び出されます
   * RewriterWorker.Resultを受け取り，Result(true)の場合は書き換えが行われたことを意味し，その場合
   * その変化でさらに書き換えが可能になった可能性があるため，`stopped`の`Rewriter`も含めすべて`queued`へ復帰させます
   * `Result(false)`を受け取った場合，それは書き換えが行われなかったことを表すので，`stopped`へ追加します
   * いずれの場合でも`working`からは取り除かれます.
   * @param worker 今回1度の書き換えが完了した`Rewriter`
   */
  protected def onWorkerOnSuccess(worker: ActorRef): PartialFunction[Any, Unit] = {
    case RewriterWorker.Result(true) => {
      modifing.lock {
        queued.put(worker)
        working -= worker
        this.requeueWorkers()
      }
    }
    case RewriterWorker.Result(false) => {
      modifing.lock {
        working -= worker
        stopped.add(worker)
      }
    }
  }

  /**
   * 書き換えを停止するまで実行します．`queued`をタイムアウト100msで参照し，
   * 要素がある場合は，`RewriterWorker.Step`メッセージを送信します．
   * `Rewriter`の処理が終了した際に`this.onWorkerOnSuccess`を呼び出すように登録し
   * 完了時の処理は`onWorkerOnSuccess`へ委譲します
   */
  def run() = {
    implicit val timeout = Timeout(5.seconds)
    import system.dispatcher
    do {
      val next = queued.poll(100, TimeUnit.MILLISECONDS)
      if (next != null) {
        working += next
        val future = next ? RewriterWorker.Step(reserve)
        future.onSuccess(this.onWorkerOnSuccess(next))
      }
    } while (!this.isStopped)
  }

  def isStopped: Boolean = modifing.lock {queued.isEmpty && working.isEmpty}

}
