package degrel.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility class for DateTime
 */
object DateTime {
  implicit val defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")

  def strftime(time: LocalDateTime, formatter: DateTimeFormatter = defaultFormatter): String = {
    time.format(formatter)
  }
}
