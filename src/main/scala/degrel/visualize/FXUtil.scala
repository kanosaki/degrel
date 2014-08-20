package degrel.visualize

import java.nio.file.Paths
import java.util.concurrent.LinkedBlockingQueue
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

import degrel.misc.CachedClassLoader
import degrel.utils.toRunnable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


/**
 * JavaFXのためのユーティリティ関数群
 */
object FXUtil {
  val FXML_DIRECTORY = Paths.get("/degrel/visualize/view").toString
  /**
   * JavaFXでFXMLをロードするときに使用されるClassLoaderはキャッシュを行わないため一般に低速．
   * なので，ロードされたクラスをキャッシュして高速化を図ります
   */
  var classLoader = new CachedClassLoader(FXMLLoader.getDefaultClassLoader)
  implicit val execContext = ExecutionContext.global

  /**
   * デフォルトのViewフォルダよりビューをロードします．
   * @param path ロードするFXMLの名前．FXUtil.FXML_DIRECTORYを基点とした相対パスです
   * @param controller FXMLに対するコントローラ．nullなら何も行いません
   * @tparam T FXMLルート要素の型
   * @return ロードされたJavaFXオブジェクト
   */
  def load[T <: Parent](path: String, controller: AnyRef = null): T = {
    this.loadAbsolute[T](Paths.get(FXML_DIRECTORY, path).toString)
  }

  /**
   * 絶対パスを指定してビューをロードします．
   * @param path ロードするFXMLの名前．getResourceを経由して取得されます．
   * @param controller FXMLに対するコントローラ．nullなら何も行いません
   * @tparam T FXMLルート要素の型
   * @return ロードされたJavaFXオブジェクト
   */
  def loadAbsolute[T <: Parent](path: String, controller: AnyRef = null): T = {
    val loader = new FXMLLoader(getClass.getResource(path))
    loader.setClassLoader(classLoader)
    if (controller != null) {
      loader.setController(controller)
    }
    val doc = loader.load[T]()
    controller match {
      case v: ViewBase => v.documentRoot = doc
    }
    doc
  }

  /**
   * コントローラと同じ場所にあるfxmlをロードし，Viewを初期化します
   * 例えば，com.example.SomeClassのインスタンスを渡すと，/com/example/SomeClass.fxmlをロードし
   * 渡されたインスタンスをコントローラとして登録します
   * @param controller 使用するJavaFXコントローラ
   * @tparam T FXMLルート要素の型
   * @return fxmlオブジェクト
   */
  def loadView[T <: Parent](controller: AnyRef): T = {
    if (controller == null)
      throw new NullPointerException("controller cannot be null")
    val cannonicalName = controller.getClass.getCanonicalName
    if (cannonicalName == null) {
      throw new IllegalArgumentException("Local or Anonymous object cannot use with loadView, use load instead.")
    }
    val viewname = "/" + cannonicalName.replace('.', '/') + ".fxml"
    this.loadAbsolute[T](viewname, controller)
  }

  /**
   * JavaFXのステージをロードします
   * @param controller 使用するコントローラ
   * @param fxmlPath FXMLファイルの場所．Class.getResourceを使用します．nullの場合はloadViewのルールに従いFXMLが探索されます
   * @param absolutePath trueの場合は`fxmlPath`を絶対パスとして扱います．`fxmlPath`がnullの時は無視されます．
   * @return ロードされた`Stage`
   */
  def loadStage(controller: ViewBase,
                fxmlPath: String = null,
                absolutePath: Boolean = false): Stage = {
    val root =
      if (fxmlPath == null)
        this.loadView[Parent](controller)
      else {
        if (absolutePath)
          this.loadAbsolute[Parent](fxmlPath, controller)
        else
          this.load[Parent](fxmlPath, controller)
      }
    this.runAndWait(
    {
      val scene = new Scene(root)
      val stage = new Stage()
      controller.scene = scene
      controller.stage = stage
      stage.setScene(scene)
      stage
    })
  }

  /**
   * デフォルトのjavafx Applicationのディスパッチスレッドでアクションを実行します
   */
  def runLater[T](f: => T): Unit = {
    FXManager.runLater(f)
  }

  /**
   * デフォルトのjavafx Applicationのディスパッチスレッドでアクションを実行します
   */
  def runLater[T](runnable: Runnable): Unit = {
    FXManager.runLater(runnable)
  }

  /**
   * 処理をJavaFXの Applicationスレッドで実行して，その結果を待ちます
   * @param f 実行する処理
   * @param atMost 最大で待機する時間
   * @tparam T 処理によって返される値の型
   * @return 処理による結果
   */
  def runAndWait[T](f: => T, atMost: Duration = Duration.Inf): T = {
    val fut = this.runFuture(f)
    Await.result(fut, atMost)
  }

  /**
   * 処理をJavaFXの Applicationスレッドで実行して，その結果のFutureを返します．
   * @note JavaFXではrunLaterのみを提供しているので，キューを使って結果を受け渡しします
   * @param f 実行する処理
   * @tparam T 処理によって返される値の型
   * @return 処理による結果のFuture
   */
  def runFuture[T](f: => T): Future[T] = {
    val channel = new LinkedBlockingQueue[T]()
    this.runLater(
    {
      val ret = f
      channel.put(ret)
    })
    Future({channel.take})
  }

}
