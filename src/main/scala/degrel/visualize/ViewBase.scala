package degrel.visualize

import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

class ViewBase {
  protected var _stage: Stage = null
  protected var _scene: Scene = null
  protected var _fxmlRoot: Parent = null

  protected def onStageSet(s: Stage): Unit = {}

  protected def onSceneSet(s: Scene): Unit = {}

  def onDocumentLoaded(): Unit = {}

  def stage_=(s: Stage) = {
    _stage = s
    onStageSet(s)
  }

  def scene_=(s: Scene) = {
    _scene = s
    onSceneSet(s)
  }

  def documentRoot_=(r: Parent) = {
    _fxmlRoot = r
    onDocumentLoaded()
  }


  def scene = _scene

  def stage = _stage

  def documentRoot: Parent = {
    _fxmlRoot
  }

  def onStopping() = {

  }

}
