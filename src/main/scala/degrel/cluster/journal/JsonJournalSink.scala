package degrel.cluster.journal

import java.io.{FileOutputStream, OutputStream, OutputStreamWriter}
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Calendar

import org.json4s.JsonAST.{JObject, JString}
import org.json4s.native.JsonMethods

import java.io.File

class JsonJournalSink(outputStream: OutputStream) extends JournalSink {
  val writer = new OutputStreamWriter(outputStream)

  override def sink(item: JournalPayload): Unit = {
    val timestamp = Journal.timestampFormat.format(Calendar.getInstance().getTime)
    val entry = item.toJson merge JObject("timestamp" -> JString(timestamp))
    val body = JsonMethods.compact(JsonMethods.render(entry))
    writer.write(body)
    writer.write('\n')
    writer.flush()
  }

  override def close(): Unit = {
    outputStream.close()
  }
}

object JsonJournalSink {
  val logFileFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX'.json'")

  def apply(logDir: String): JsonJournalSink = {
    val now = Calendar.getInstance()
    val fileName = logFileFormat.format(now.getTime)
    new File(logDir).mkdirs()
    val outputFile = Paths.get(logDir, fileName).toFile
    new JsonJournalSink(new FileOutputStream(outputFile))
  }
}
