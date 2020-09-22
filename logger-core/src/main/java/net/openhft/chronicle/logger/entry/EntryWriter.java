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
        return add(builder,
                secs,
                nanos,
                level,
                loggerName,
                threadName,
                contentBuf);
    }

    public ByteBuffer write(FlatBufferBuilder builder,
                                long secs,
                                int nanos,
                                int level,
                                String loggerName,
                                String threadName,
                                ByteBuffer contentBuf,
                                String contentType, // XXX better as ByteBuffer?
                                String contentEncoding) {
        return add(builder,
                secs,
                nanos,
                level,
                loggerName,
                threadName,
                contentBuf,
                contentType,
                contentEncoding);
    }

    private ByteBuffer add(FlatBufferBuilder builder,
                   long epochSecond,
                   int nanos,
                   int level,
                   String loggerName,
                   ByteBuffer contentBuffer) {
        int name = builder.createString(loggerName);
        int content = builder.createByteVector(contentBuffer);

        Entry.startEntry(builder);
        addRequired(builder,  epochSecond, nanos, name, level, content);
        Entry.finishEntryBuffer(builder, Entry.endEntry(builder));
        return builder.dataBuffer();
    }

    private ByteBuffer add(FlatBufferBuilder builder,
                   long epochSecond,
                   int nanos,
                   int level,
                   String loggerName,
                   String threadName,
                   ByteBuffer contentBuffer) {
        int nameOffset = builder.createString(loggerName);

        // Use shared strings if available
        int threadNameOffset = builder.createString(threadName);
        int contentOffset = builder.createByteVector(contentBuffer);

        Entry.startEntry(builder);
        addRequired(builder, epochSecond, nanos, level, nameOffset, contentOffset);
        Entry.addThreadName(builder, threadNameOffset);
        Entry.finishEntryBuffer(builder, Entry.endEntry(builder));
        return builder.dataBuffer();
    }

    protected ByteBuffer add(FlatBufferBuilder builder,
                   long epochSecond,
                   int nanos,
                   int level,
                   String loggerName,
                   String threadName,
                   ByteBuffer contentBuffer,
                   String contentType,
                   String contentEncoding) {
        int name = builder.createString(loggerName);

        // Use shared strings if available
        int threadNameOffset = builder.createString(threadName);
        int content = builder.createByteVector(contentBuffer);
        int type = builder.createString(contentType);
        int encoding = builder.createString(contentEncoding);

        Entry.startEntry(builder);
        addRequired(builder, epochSecond, nanos, level, name, content);
        Entry.addThreadName(builder, threadNameOffset);
        Entry.addContentType(builder, type);
        Entry.addContentEncoding(builder, encoding);
        Entry.finishEntryBuffer(builder, Entry.endEntry(builder));
        return builder.dataBuffer();
    }

    protected void addRequired(FlatBufferBuilder builder,
                               long epochSecond,
                               int nanos,
                               int level,
                               int nameOffset,
                               int contentOffset) {
        Entry.addTimestamp(builder, EntryTimestamp.createEntryTimestamp(builder, epochSecond, nanos));
        Entry.addLevel(builder, level);
        Entry.addLoggerName(builder, nameOffset);
        Entry.addContent(builder, contentOffset);
    }
}
