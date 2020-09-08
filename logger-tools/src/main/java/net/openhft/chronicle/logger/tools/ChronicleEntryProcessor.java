package net.openhft.chronicle.logger.tools;

import java.util.function.Function;

@FunctionalInterface
public interface ChronicleEntryProcessor extends Function<ChronicleLogEvent, String> {
}
