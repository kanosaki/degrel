package degrel.visualize

import java.util.concurrent.{CountDownLatch, TimeUnit}
import javafx.application.{Application, Platform}
import javafx.stage.Stage

import scala.concurrent.{ExecutionContext, Future}
import scala.ref.WeakReference

class FXManager extends Application {
  FXManager.__initialize_lock.synchronized(
  {
    if (FXManager.instance != null) {
      throw new IllegalStateException("You cannot initialize FXManager twice")
    }
    FXManager.instance = this
  })

  var primaryStage: Stage = null
  private var childControllers = List[WeakReference[ViewBase]]()
  private val childControllersLock = new Object()

  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage

    // Finish Initialization
    FXManager.initLatch.countDown()
  }

  override def init() = {

  }

  override def stop() = {
    for (child <- this.childControllers.filter(_.get.nonEmpty)) {
      child.get match {
        case Some(view) => view.onStopping()
        case None =>
      }
    }
  }

  def addChildController(view: ViewBase) = {
    childControllersLock.synchronized(
    {
      this.childControllers = new WeakReference[ViewBase](view) ::
                              childControllers.filter(_.get.nonEmpty)
    })
  }

  /**
   * Use FXUtil.runLater
   */
  def runLater(runnable: Runnable) = {
    Platform.runLater(runnable)
  }
}

/**
 * JavaFX用の管理モジュール．`FXUtil`へjavafx Applicationの機能を提供します.
 * JavaFXでは`javafx.application.Application`をアプリケーションのEntry Pointとすることを前提としていますが
 * それでは不便なのでdegrelのいろいろなモジュールや，さらにはデバッグ中にIDE等からも視覚化機能を利用できるように橋渡しを行います．
 * 具体的にはjavafx Applicationをシングルトンとして保持していて，javafx Application内で実行が必要な物へのアクセスを提供します．
 * 実際には`FXUtil`の各ユーティリティ関数群を通して利用してください．
 */
object FXManager {
  private val __launch_lock = new Object()
  private val __initialize_lock = new Object()
  private var instance: FXManager = null
  private val initLatch = new CountDownLatch(1)
  private var __is_launching = false
  protected var _isLaunchd = false
  implicit private val ec = ExecutionContext.global

  def isLaunchd = _isLaunchd

  def registerController(view: ViewBase) = {
    instance match {
      case null =>
      case _ => instance.addChildController(view)
    }
  }

  /**
   * JavaFX Applicationを非同期で初期化します．
   * JavaFXでは1度しか呼べないため，2度目以降は何もしません．実際に処理が行われたかどうかは返値を見てください．
   * @param args 引数
   * @return 実際にApplication.launchが実行された場合はtrue, 2回目以降なので実行されなかった場合はfalse
   */
  def launchAsync(args: Array[String] = Array()): Boolean = {
    __launch_lock.synchronized(
    {
      if (__is_launching) return false
      __is_launching = true
      val ret = _isLaunchd match {
        case true => false
        case false => {
          Future(
          {
            Application.launch(classOf[FXManager], args: _*)
          })
          true
        }
      }
      __is_launching = false
      ret
    })
  }

  def launch(args: Array[String] = Array()): Boolean = {
    val ret = this.launchAsync(args)
    this.waitForInitialize()
    ret
  }

  def waitForInitialize(timeout: Long = -1,
                        unit: TimeUnit = TimeUnit.MILLISECONDS) = {
    if (timeout < 0) {
      initLatch.await()
    } else {
      initLatch.await(timeout, unit)
    }
  }


  /**
   * Use FXUtil.runLater
   */
  def runLater(runnable: Runnable) = {
    if (!isLaunchd) {
      FXManager.launch()
    }
    instance.runLater(runnable)
  }
}
