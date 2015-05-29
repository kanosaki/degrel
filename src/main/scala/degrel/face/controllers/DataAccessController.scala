package degrel.face.controllers

import degrel.face.messages.GraphQueryRequest
import spray.routing.RequestContext

/**
 * 処理系内部のデータにアクセス出来るようにします
 */
class DataAccessController {
  def query(ctx: RequestContext, q: GraphQueryRequest) = {
    ctx.complete("FOOBAR")
  }
}
