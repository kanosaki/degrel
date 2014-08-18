package degrel.visualize.fxapp

import java.util.concurrent.{CountDownLatch, TimeUnit}
import javafx.application.{Application, Platform}
import javafx.stage.Stage

import scala.concurrent.{ExecutionContext, Future}

class FXManager extends Application {
  FXManager.__initialize_lock.synchronized(
  {
    if (FXManager.instance != null) {
      throw new IllegalStateException("You cannot initialize FXManager twice")
    }
    FXManager.instance = this
  })

  var primaryStage: Stage = null

  override def start(primaryStage: Stage): Unit = {
    this.primaryStage = primaryStage

    // Finish Initialization
    FXManager.initLatch.countDown()
  }

  override def init() = {

  }

  /**
   * Use FXUtil.runLater
   */
  def runLater(runnable: Runnable) = {
    Platform.runLater(runnable)
  }
}

object FXManager {
  private val __launch_lock = new Object()
  private val __initialize_lock = new Object()
  private var instance: FXManager = null
  private val initLatch = new CountDownLatch(1)
  protected var _isLaunchd = false
  implicit private val ec = ExecutionContext.global

  def waitForInitialize(timeout: Long = -1,
                        unit: TimeUnit = TimeUnit.MILLISECONDS) = {
    if (timeout < 0) {
      initLatch.await()
    } else {
      initLatch.await(timeout, unit)
    }
  }

  def isLaunchd = _isLaunchd

  /**
   * JavaFX Applicationを初期化します．JavaFXでは1度しか呼べないため，
   * @param args 引数
   * @return 実際にApplication.launchが実行された場合はtrue, 2回目以降なので実行されなかった場合はfalse
   */
  def launchAsync(args: Array[String] = Array()): Boolean = {
    __launch_lock.synchronized(
    {
      _isLaunchd match {
        case true => false
        case false => {
          Future(
          {
            Application.launch(classOf[FXManager], args: _*)
          })
          true
        }
      }
    })
  }

  def launch(args: Array[String] = Array()): Boolean = {
    val ret = this.launchAsync(args)
    this.waitForInitialize()
    ret
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
