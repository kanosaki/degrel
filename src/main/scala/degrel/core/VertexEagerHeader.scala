package degrel.core

import degrel.engine.{VertexMatching, MatchingContext}

class VertexEagerHeader(protected var _body: VertexBody) extends VertexHeader(_ => _body) {

}
