package indix.rocksdb

object TimerUtil {

  def timed[T](report: (Long) => Unit)(block: => T): (Long, T) = {
    val start = System.nanoTime()
    val result = block
    val timeTookInMillis = (System.nanoTime() - start) / (1000 * 1000)

    report(timeTookInMillis)
    (timeTookInMillis, result)
  }
}
