package net.openhft.chronicle.logger.entry;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;

public class EntryWriter {

    public ByteBuffer write(FlatBufferBuilder builder,
                            long secs,
                            int nanos,
                            int level,
                            String loggerName,
                            String threadName,
                            ByteBuffer contentBuf) {
        int name = builder.createString(loggerName);
        int content = builder.createByteVector(contentBuf);
        int threadNameOffset = builder.createString(threadName);
        Entry.startEntry(builder);
        Entry.addTimestamp(builder, EntryTimestamp.createEntryTimestamp(builder, secs, nanos));
        Entry.addLevel(builder, level);
        Entry.addLoggerName(builder, name);
        Entry.addContent(builder, content);
        Entry.addThreadName(builder, threadNameOffset);
        Entry.finishEntryBuffer(builder, Entry.endEntry(builder));
        return builder.dataBuffer();
    }
}
