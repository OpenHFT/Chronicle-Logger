package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleEntryProcessor;
import net.openhft.chronicle.logger.DefaultChronicleEntryProcessor;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.queue.ChronicleQueue;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        ChronicleEntryProcessor<String> entryProcessor = new DefaultChronicleEntryProcessor(registry);
        ChronicleLogProcessor logProcessor = e -> {
            String content = entryProcessor.apply(e);
            System.out.println(content);
        };
        logProcessor.processLogs(cq, reader, waitForIt);
    }

}
