package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.EntryTransformer;
import net.openhft.chronicle.logger.DefaultEntryTransformer;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ChronicleOutput {
    private final Path directory;
    private final ChronicleQueue cq;

    public ChronicleOutput(ChronicleQueue cq, Path directory) {
        this.cq = cq;
        this.directory = directory;
    }

    public void process(boolean waitForIt) {
        CodecRegistry registry = CodecRegistry.builder().withDefaults(directory).build();
        LogAppenderConfig config = LogAppenderConfig.parse(directory);
        Codec codec = registry.find(config.getContentEncoding());
        EntryTransformer<String> entryTransformer = new DefaultEntryTransformer(codec);
        ChronicleLogProcessor logProcessor = e -> {
            String content = entryTransformer.apply(e);
            System.out.println(content);
        };
        EntryReader reader = new EntryReader();
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
