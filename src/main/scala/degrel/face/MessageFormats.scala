package degrel.face

import org.json4s.{DefaultFormats, Formats}
import spray.httpx.Json4sSupport

object MessageFormats extends Json4sSupport {
  override implicit def json4sFormats: Formats = DefaultFormats
}
