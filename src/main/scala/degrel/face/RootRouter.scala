package degrel.face

import degrel.face.controllers.{DataAccessController, StatusController}
import scaldi.{Injectable, Injector}
import spray.routing.{HttpService, Route}
import scala.language.postfixOps

trait RootRouter extends HttpService with Injectable {
  implicit val inj: Injector

  import degrel.face.MessageFormats._

  lazy val repo = inject[FaceRepository]

  /**
   * 処理系内部のグラフデータにアクセスします
   */
  lazy val universe = {
    val ctrlr = inject[DataAccessController]
    slash(
      path("query") {
        get {
          parameters('id.as[Int] ?, 'label.as[String] ?).
            as(messages.GraphQueryRequest) { q => ctx =>
            ctrlr.query(ctx, q)
          }
        }
      },
      path("items") {
        complete("")
      }
    )
  }

  /**
   * 処理系の動作状態にアクセスします
   * NOTE: `status`はコンフリクトを起こすため，statusRという名前にしています
   */
  lazy val statusR = {
    val ctrlr = inject[StatusController]
    get {
      complete(messages.FaceStatus(success = true))
    }
  }

  lazy val root = slash(
    pathPrefix("status")(statusR),
    pathPrefix("universe")(universe)
  )


  def slash(ds: Route*) = {
    ds.reduce(_ ~ _)
  }
}
