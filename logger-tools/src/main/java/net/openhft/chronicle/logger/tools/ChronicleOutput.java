package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.EntryProcessor;
import net.openhft.chronicle.logger.DefaultEntryProcessor;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ChronicleOutput {
    private final Path parent;
    private final ChronicleQueue cq;

    public ChronicleOutput(ChronicleQueue cq, Path parent) {
        this.cq = cq;
        this.parent = parent;
    }

    public void process(boolean waitForIt) {
        EntryReader reader = new EntryReader();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        Codec codec = registry.find(CodecRegistry.ZSTANDARD);
        EntryProcessor<String> entryProcessor = new DefaultEntryProcessor(codec);
        ChronicleLogProcessor logProcessor = e -> {
            String content = entryProcessor.apply(e);
            System.out.println(content);
        };
        logProcessor.processLogs(cq, reader, waitForIt);
    }

    protected Charset getCharset(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    String charset = param.split("=", 2)[1];
                    return Charset.forName(charset);
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

}
