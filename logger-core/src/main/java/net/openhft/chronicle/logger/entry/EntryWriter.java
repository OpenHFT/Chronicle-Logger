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
        addRequired(builder, secs, nanos, level, name, content);
        Entry.addThreadName(builder, threadNameOffset);
        Entry.finishEntryBuffer(builder, Entry.endEntry(builder));
        return builder.dataBuffer();
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
        // required strings and bytes
        int name = builder.createString(loggerName);
        int content = builder.createByteVector(contentBuf);

        // optional strings should only be created if not null
        int threadNameOffset = (threadName != null) ? builder.createString(threadName) : 0;
        int type = (contentType == null) ? 0 : builder.createString(contentType);
        int encoding = (contentEncoding == null) ? 0 : builder.createString(contentEncoding);

        Entry.startEntry(builder);
        addRequired(builder, secs, nanos, level, name, content);
        if (threadName != null) {
            Entry.addThreadName(builder, threadNameOffset);
        }
        if (contentType != null) {
            Entry.addContentType(builder, type);
        }
        if (contentEncoding != null) {
            Entry.addContentEncoding(builder, encoding);
        }
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
