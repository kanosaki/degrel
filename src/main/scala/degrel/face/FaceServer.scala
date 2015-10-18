package degrel.face

import java.awt.Desktop
import java.net.URI

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import scaldi.Injector
import spray.can.Http

import scala.concurrent.duration._

class FaceServer(implicit inj: Injector) {
  implicit val system = ActorSystem("faceServer")
  implicit val timeout = Timeout(5.seconds)

  val service = {
    val prop = Props(classOf[RootActor], inj)
    system.actorOf(prop, "degrel-face")
  }

  def start(openBrowser: Boolean = false) = {
    IO(Http) ? Http.Bind(
      service,
      interface = "localhost",
      port = 4000
    )
    if (openBrowser && Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI("http://localhost:4000"))
    }
  }
}
