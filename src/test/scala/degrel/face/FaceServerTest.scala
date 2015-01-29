package degrel.face

import degrel.face.controllers.{DataAccessController, StatusController}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import scaldi.Module
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.testkit.ScalatestRouteTest

class FaceServerTest
  extends FlatSpec
  with ScalatestRouteTest
  with Matchers
  with MockFactory
  with Module
  with RootRouter {

  class TestModule extends Module {
    bind[FaceRepository] to new FaceRepository()
    bind[DataAccessController] to new DataAccessController()
    bind[StatusController] to new StatusController()
  }
  implicit val inj: TestModule = new TestModule()

  import degrel.face.MessageFormats._

  def actorRefFactory = system

  "status" should "return empty response" in {
    Get("/status") ~> root ~> check {
      assert(contentType.mediaType === `application/json`)
      val res = responseAs[messages.FaceStatus]
      assert(res.success)
      assert(status === OK)
    }
  }

  "universe/query" should "return empty if query result is empty" in {
    Get("/universe/query?id=4&id=5") ~> root ~> check {
      assert(status == OK)
    }
  }
}
