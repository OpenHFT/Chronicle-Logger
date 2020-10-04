package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.EntryTransformer;
import net.openhft.chronicle.logger.codec.Codec;
import net.openhft.chronicle.logger.entry.EntryReader;
import net.openhft.chronicle.logger.DefaultEntryTransformer;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.WireType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ChronicleLogReaderTest {
    @Before
    public void setup() {
        System.setProperty(
                "logback.configurationFile",
                System.getProperty("resources.path")
                        + "/logback-chronicle-binary-appender.xml");
    }

    @Test
    public void readTest() {
        final Logger logger = LoggerFactory.getLogger("binary-chronicle");
        logger.info("test {} {} {}", 1, 100L, 100.123D);
        logger.info("test {} {} {}", 2, 100L, 100.123D);
        logger.info("test {} {} {}", 3, 100L, 100.123D);

        String path = System.getProperty("java.io.tmpdir") + "/chronicle-logback/binary-chronicle";
        ChronicleQueue cq = ChronicleQueue.singleBuilder(path).wireType(WireType.BINARY_LIGHT).build();
        EntryReader reader = new EntryReader();
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        Codec codec = registry.find("identity");
        EntryTransformer<String> entryTransformer = new DefaultEntryTransformer(codec);
        ChronicleLogProcessor logProcessor = e -> System.out.println(entryTransformer.apply(e));
        logProcessor.processLogs(cq, reader, false);

        //ChroniCat.main(new String[] {System.getProperty("java.io.tmpdir") + "/chronicle-logback/binary-chronicle"});
    }
}
