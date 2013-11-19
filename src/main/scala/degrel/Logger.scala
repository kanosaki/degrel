package degrel

/**
 * 実際にログを行うライブラリとのアダプター
 */
trait Logger extends com.typesafe.scalalogging.slf4j.Logging {
  def debug(msg: String) = logger.debug(msg)
  def info(msg: String) = logger.info(msg)
  def warn(msg: String) = logger.warn(msg)
  def error(msg: String) = logger.error(msg)
}
