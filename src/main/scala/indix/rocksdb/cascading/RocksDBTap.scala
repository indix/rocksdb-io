package indix.rocksdb.cascading

import cascading.flow.FlowProcess
import cascading.scheme.{Scheme, SinkCall, SourceCall}
import cascading.tap.Tap
import cascading.tap.hadoop.Hfs
import cascading.tuple.Fields
import indix.rocksdb.RKeyValueWritable
import indix.rocksdb.hadoop.RocksDBOutputFormat
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.{JobConf, OutputCollector, RecordReader}


class RocksDBTap(root: String) extends Hfs {

  setStringPath(root)

  private val sinkFields: Fields = new Fields("shard", "keyvalue")
  setScheme(new RocksDBTupleScheme().asInstanceOf[Scheme[JobConf, RecordReader[_, _], OutputCollector[_, _], _, _]])

  class RocksDBTupleScheme() extends Scheme[JobConf, RecordReader[Text, RKeyValueWritable], OutputCollector[Text, RKeyValueWritable], Array[Object], Array[Object]](Fields.ALL, sinkFields) {
    // Ignore source* for now
    override def sourceConfInit(flowProcess: FlowProcess[JobConf], tap: Tap[JobConf, RecordReader[Text, RKeyValueWritable], OutputCollector[Text, RKeyValueWritable]], conf: JobConf): Unit = ???

    override def source(flowProcess: FlowProcess[JobConf], sourceCall: SourceCall[Array[Object], RecordReader[Text, RKeyValueWritable]]): Boolean = ???

    override def sinkConfInit(flowProcess: FlowProcess[JobConf], tap: Tap[JobConf, RecordReader[Text, RKeyValueWritable], OutputCollector[Text, RKeyValueWritable]], conf: JobConf): Unit = {
      conf.setOutputFormat(classOf[RocksDBOutputFormat])
    }

    override def sink(flowProcess: FlowProcess[JobConf], sinkCall: SinkCall[Array[Object], OutputCollector[Text, RKeyValueWritable]]): Unit = {
      val tuple = sinkCall.getOutgoingEntry
      val shard = tuple.getObject(0).asInstanceOf[Text]
      val keyValue = tuple.getObject(1).asInstanceOf[RKeyValueWritable]

      sinkCall.getOutput.collect(shard, keyValue)
    }
  }

}
