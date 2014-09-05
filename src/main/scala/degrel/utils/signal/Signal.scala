package degrel.utils.signal

trait Signal[T] {
  type Handler = (Any, T) => Unit

  /**
   * シグナルの購読をします
   * @param handler イベントハンドラ
   */
  def register(handler: Handler): Unit

  /**
   * シグナルの購読を停止します
   * @param handler イベントハンドラ
   */
  def unregister(handler: Handler): Unit

  /**
   * `register`へのエイリアス
   */
  def +=(handler: Handler) = register(handler)

  /**
   * `unregister`へのエイリアス
   */
  def -=(handler: Handler) = unregister(handler)

  /**
   * シグナルを登録しているハンドラへ送信します
   * @param obj 引数
   * @throws InterruptedException タイムアウトが事前に設定されるとき，時間を超過すると`InterruptedException`が発生します
   */
  def trigger(sender: Any, obj: T): Unit
}
