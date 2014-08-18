package degrel.visualize.fxapp

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class MultiGraphViewApp extends Application {
  override def start(stage: Stage): Unit = {
    if(MultiGraphViewApp.s == null) {
      val location = getClass.getResource("/degrel/visualize/fxml/MultiGraphView.fxml")
      val rootPane = FXMLLoader.load(location).asInstanceOf[Pane]
      MultiGraphViewApp.s = new Scene(rootPane)
    }
    stage.setScene(MultiGraphViewApp.s)
    stage.show()
  }
}

object MultiGraphViewApp {
  var s: Scene = null
}
