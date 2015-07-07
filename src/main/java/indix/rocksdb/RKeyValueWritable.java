package indix.rocksdb;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Writable;

import java.io.*;

public class RKeyValueWritable implements Writable, Externalizable {

    private BytesWritable key;
    private BytesWritable value;

    public RKeyValueWritable() {
    }

    public RKeyValueWritable(BytesWritable key, BytesWritable value) {
        this.key = key;
        this.value = value;
    }

    public BytesWritable getKey() {
        return key;
    }

    public byte[] keyInBytes() {
        return getKey().getBytes();
    }

    public RKeyValueWritable setKey(BytesWritable key) {
        this.key = key;
        return this;
    }

    public BytesWritable getValue() {
        return value;
    }

    public byte[] valueInBytes() {
        return getValue().getBytes();
    }

    public RKeyValueWritable setValue(BytesWritable value) {
        this.value = value;
        return this;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(key.getLength());
        out.write(key.getBytes());

        out.writeInt(value.getLength());
        out.write(value.getBytes());
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        byte[] keyBytes = new byte[in.readInt()];
        in.readFully(keyBytes);
        key = new BytesWritable(keyBytes);

        byte[] valueBytes = new byte[in.readInt()];
        in.readFully(valueBytes);
        value = new BytesWritable(valueBytes);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        write(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readFields(in);
    }
}
