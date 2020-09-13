package net.openhft.chronicle.logger;

import java.util.function.Function;

/**
 * Turns a log entry into a given type, usually serializable, i.e.
 * {@code String} or {@code CharSequence}.
 *
 * @param <T> the type.
 */
@FunctionalInterface
public interface ChronicleEntryProcessor<T> extends Function<ChronicleLogEvent, T> {
}
