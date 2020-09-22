package net.openhft.chronicle.logger;

import net.openhft.chronicle.logger.entry.Entry;

import java.util.function.Function;

/**
 * Turns a log entry into a given type, usually serializable, i.e.
 * {@code String} or {@code CharSequence}.
 *
 * @param <T> the type.
 */
@FunctionalInterface
public interface ChronicleEntryProcessor<T> extends Function<Entry, T> {
}
