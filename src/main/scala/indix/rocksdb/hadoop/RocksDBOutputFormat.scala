package indix.rocksdb.hadoop

import java.nio.file.Files

import indix.rocksdb.{RKeyValueWritable, TimerUtil}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred._
import org.apache.hadoop.util.Progressable
import org.rocksdb.{Options, RocksDB, WriteBatch, WriteOptions}
import org.slf4j.LoggerFactory

import scala.collection.mutable

class RocksDBOutputFormat extends FileOutputFormat[Text, RKeyValueWritable] {
  override def getRecordWriter(fileSystem: FileSystem, jobConf: JobConf, s: String, progressable: Progressable): RecordWriter[Text, RKeyValueWritable] = new RocksDBRecordWriter(jobConf)
}

class RocksDBRecordWriter(conf: JobConf) extends RecordWriter[Text, RKeyValueWritable] {
  val LOGGER = LoggerFactory.getLogger(getClass)
  val baseOutput = conf.get("mapred.output.dir")
  val writeBatchSize = conf.getInt("rocksdb.write.batch.size", 25000)
  val tempDir = Files.createTempDirectory("DBshards")
  var db: RocksDB = _
  val recordsToBeFlushed = mutable.ListBuffer[RKeyValueWritable]()
  var previousShard: Option[Text] = None

  override def write(shard: Text, keyValue: RKeyValueWritable): Unit = {
    previousShard match {
      case None =>
        db = openDB(shard)
        previousShard = Some(shard)
      case Some(old) if !old.equals(shard) =>
        flush()
        closeDb()
        db = openDB(shard)
        previousShard = Some(shard)
      case Some(old) if recordsToBeFlushed.nonEmpty && recordsToBeFlushed.size % writeBatchSize == 0 => flush()
      case _ =>
    }
    recordsToBeFlushed += keyValue
  }

  def flush() {
    TimerUtil.timed { timeInMillis => LOGGER.info(s"Flushing ${recordsToBeFlushed.size} records in batch took - $timeInMillis ms") } {
      val numberOfRecords = recordsToBeFlushed.size
      LOGGER.info(s"Starting to flush $numberOfRecords records")
      val writeBatch = new WriteBatch()
      recordsToBeFlushed.foreach(r => writeBatch.put(r.getKey.getBytes, r.getValue.getBytes))
      db.write(new WriteOptions().setDisableWAL(true), writeBatch)
      recordsToBeFlushed.clear()
      LOGGER.info(s"Completed flushing $numberOfRecords records")
    }
  }

  def openDB(shard: Text): RocksDB = {
    val options = new Options()
      .setMaxBackgroundCompactions(2)
      .setCreateIfMissing(true)
    //.setMaxBytesForLevelBase(128 * 1024 * 1024)
    RocksDB.open(options, s"$tempDir/${shard.toString}")
  }

  def closeDb() = {
    if (db != null) {
      LOGGER.info(s"RocksDB stats for ${previousShard.getOrElse("N/A")}")
      LOGGER.info(db.getProperty("rocksdb.stats"))
      db.close()
    }
  }

  def close(reporter: Reporter): Unit = {
    closeDb()
    LOGGER.info("Starting to copy files from localFS to destination")
    val fs = FileSystem.get(conf)
    tempDir.toFile.listFiles().foreach { file =>
      fs.copyFromLocalFile(new Path(file.getPath), new Path(baseOutput))
    }
    LOGGER.info("Finished copying the files to destination")
  }
}
