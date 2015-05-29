package degrel.face

import scaldi.Module


// Cake pattern
/**
 * IDEのデバッガから呼ばれた場合の調整や
 * 一般的なセットアップを行います
 */
class ServerLauncher(val repo: FaceRepository) extends Module {

  // Dependency Bindings
  bind[FaceRepository] to repo

  private var _instance: FaceServer = null

  def server: FaceServer = {
    if (_instance == null) {
      this.start()
      bind[FaceServer] to _instance
    }
    _instance
  }

  def start() = {
    _instance = new FaceServer()
  }
}
