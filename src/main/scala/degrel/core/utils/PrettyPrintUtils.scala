package degrel.core.utils

import java.util.Locale

import degrel.core.{LocalVertexBody, LocalVertexHeader}


object PrettyPrintUtils {
  val FixedShortenTypeNames: Map[Class[_], String] = Map(
    classOf[LocalVertexHeader] -> "",
    classOf[LocalVertexBody] -> ""
  )

  def shortenTypeName(obj: Any): String = {
    val cls = obj.getClass
    if (FixedShortenTypeNames.contains(cls)) {
      FixedShortenTypeNames(cls)
    } else {
      val name = cls.getSimpleName
      name.filter(_.isUpper).toLowerCase(Locale.US)
    }
  }
}