package degrel

import degrel.core.Vertex

/**
 * Web経由でデバッグや設定の変更等を可能にします
 */
package object face {
  val repository = new FaceRepository()

  private[this] val launcher = new ServerLauncher(repository)

  def server = launcher.server

  def expose(v: Vertex) = {
    repository.expose(v)
  }
}
