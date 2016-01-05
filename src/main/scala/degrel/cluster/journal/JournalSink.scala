package degrel.cluster.journal

trait JournalSink {
  def sink(item: JournalPayload): Unit
  def close(): Unit
}
