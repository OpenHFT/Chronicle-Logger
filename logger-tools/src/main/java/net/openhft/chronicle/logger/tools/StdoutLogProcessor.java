package net.openhft.chronicle.logger.tools;

import org.jetbrains.annotations.NotNull;

/**
 * Prints out newline delimited entries to
 */
public class StdoutLogProcessor implements ChronicleLogProcessor {

    private final ChronicleEntryProcessor processor;

    public StdoutLogProcessor(@NotNull ChronicleEntryProcessor entryProcessor) {
        this.processor = entryProcessor;
    }

    @Override
    public void process(ChronicleLogEvent e) {
        System.out.println(processor.apply(e));
    }
}
