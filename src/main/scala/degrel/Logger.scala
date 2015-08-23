package degrel

import com.typesafe.scalalogging.{LazyLogging, Logger => SLogger}
import org.slf4j.LoggerFactory

/**
 * 実際にログを行うライブラリとのアダプター
 */
trait Logger extends LazyLogging {
  def getLogger(name: String): SLogger = SLogger(LoggerFactory.getLogger(name))
}
