package degrel.core

import degrel.engine.{VertexMatching, MatchingContext}

class VertexHeader(protected var _body: VertexBody) extends VertexLazyHeader(_ => _body) {

}
