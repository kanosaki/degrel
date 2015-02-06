package degrel.ui

import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class ViewBase {
  protected var _stage: Stage = null
  protected var _scene: Scene = null
  protected var _fxmlRoot: Parent = null

  def scene = _scene

  def scene_=(s: Scene) = {
    _scene = s
    onSceneSet(s)
  }

  protected def onSceneSet(s: Scene): Unit = {}

  def stage = _stage

  def stage_=(s: Stage) = {
    _stage = s
    onStageSet(s)
  }

  protected def onStageSet(s: Stage): Unit = {}

  def documentRoot: Parent = {
    _fxmlRoot
  }

  def documentRoot_=(r: Parent) = {
    _fxmlRoot = r
    onDocumentLoaded()
  }

  def onDocumentLoaded(): Unit = {}

  def onStopping() = {

  }

}
