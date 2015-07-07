package indix.rocksdb.scalding

import java.io.{InputStream, OutputStream}
import java.util.Properties

import cascading.scheme.Scheme
import cascading.tap.Tap
import cascading.tuple.Fields
import com.twitter.scalding._
import indix.rocksdb.cascading.RocksDBTap
import org.apache.hadoop.mapred.{JobConf, OutputCollector, RecordReader}

case class RocksDBSink(source: String, sourceSeperator: String = " ", field: Fields = Fields.ALL) extends FixedPathSource(source.split(sourceSeperator): _*) with FieldConversions {
  override val hdfsScheme: Scheme[JobConf, RecordReader[_, _], OutputCollector[_, _], _, _] = HadoopSchemeInstance(new cascading.scheme.hadoop.TextLine())
  hdfsScheme.setSourceFields("shard", "keyvalue")

  override val localScheme = new cascading.scheme.local.TextLine().asInstanceOf[Scheme[Properties, InputStream, OutputStream, _, _]]
  localScheme.setSourceFields("shard", "keyvalue")


  override def createTap(readOrWrite: AccessMode)(implicit mode: Mode): Tap[_, _, _] = {
    mode match {
      case Hdfs(_, _) => new RocksDBTap(hdfsWritePath).asInstanceOf[Tap[_, _, _]]


      case Local(_) => new RocksDBTap(hdfsWritePath).asInstanceOf[Tap[_, _, _]]


      case _ => super.createTap(Write)(mode)
    }
  }

  override def hdfsPaths = source.split(sourceSeperator).toList

  override def localPath = source
}


