/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.threads.Pauser;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration for creating Chronicle Queues used by log appenders.
 *
 * <p>Options mirror the {@link SingleChronicleQueueBuilder} settings so that
 * queues created by {@link #build(String, String)} have a predictable layout.</p>
 */
public class LogAppenderConfig {

    private static final String[] KEYS = {
            "blockSize",
            "bufferCapacity",
            "rollCycle"
    };

    /** Size in bytes of each queue data block. */
    private int blockSize;

    /** Capacity in bytes of the queue write buffer. */
    private long bufferCapacity;

    /** Name of the {@link RollCycles} to use when rolling files. */
    private String rollCycle;

    /**
     * Creates an empty configuration with default values.
     */
    public LogAppenderConfig() {
    }

    /**
     * Returns the configured block size in bytes.
     *
     * @return block size
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Sets the queue block size in bytes.
     *
     * @param blockSize size of each queue block
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    /**
     * Returns the configured write buffer capacity.
     *
     * @return buffer capacity in bytes
     */
    public long getBufferCapacity() {
        return this.bufferCapacity;
    }

    /**
     * Sets the write buffer capacity.
     *
     * @param bufferCapacity capacity in bytes
     */
    public void setBufferCapacity(long bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    /**
     * Returns the roll cycle name to use.
     *
     * @return roll cycle enum name, or {@code null} when unset
     */
    public String getRollCycle() {
        return rollCycle;
    }

    /**
     * Sets the roll cycle name.
     *
     * @param rollCycle roll cycle enum name
     */
    public void setRollCycle(String rollCycle) {
        this.rollCycle = rollCycle;
    }

    /**
     * Lists the property keys this configuration recognises.
     *
     * @return recognised property names
     */
    public String[] keys() {
        return KEYS;
    }

    /**
     * Builds a queue at {@code path} using this configuration.
     *
     * @param path directory for the queue
     * @param wireType name of the wire format or {@code null} for binary
     * @return the configured queue
     */
    public ChronicleQueue build(String path, String wireType) {
        // trigger Pauser to load its classes
        Pauser.getBalanced();

        WireType wireTypeEnum = wireType != null ? WireType.valueOf(wireType.toUpperCase()) : WireType.BINARY_LIGHT;
        SingleChronicleQueueBuilder builder = ChronicleQueue.singleBuilder(path)
                .wireType(wireTypeEnum)
                .blockSize(blockSize)
                .bufferCapacity(bufferCapacity);
        if (rollCycle != null)
            builder.rollCycle(RollCycles.valueOf(rollCycle));
        return builder.build();
    }

    /**
     * Reads configuration values from a {@link Properties} object.
     * Only keys starting with {@code prefix}, when supplied, are considered.
     *
     * @param properties property source
     * @param prefix     prefix to strip, or {@code null} to read all keys
     */
    public void setProperties(@NotNull final Properties properties, @Nullable final String prefix) {
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String name = entry.getKey().toString();
            final String value = entry.getValue().toString();

            if (prefix != null && !prefix.isEmpty()) {
                if (name.startsWith(prefix)) {
                    setProperty(name.substring(prefix.length()), value);
                }
            } else {
                setProperty(name, value);
            }
        }
    }

    /**
     * Sets a single property by reflection. Unknown properties are ignored.
     *
     * @param propName  property name (after any prefix has been removed)
     * @param propValue string value to assign
     */
    public void setProperty(@NotNull final String propName, final String propValue) {
        try {
            final PropertyDescriptor property = new PropertyDescriptor(propName, this.getClass());
            final Method method = property.getWriteMethod();
            final Class<?> type = method.getParameterTypes()[0];

            if (type == null || propValue == null || propValue.isEmpty()) {
                return;
            }
            if (type == int.class) {
                method.invoke(this, Integer.parseInt(propValue));

            } else if (type == long.class) {
                method.invoke(this, Long.parseLong(propValue));

            } else if (type == boolean.class) {
                method.invoke(this, Boolean.parseBoolean(propValue));

            } else if (type == String.class) {
                method.invoke(this, propValue);
            }
        } catch (Exception e) {
            System.err.printf("Unable to set property '%s' to '%s': %s%n",
                    propName, propValue, e.getMessage());
        }
    }
}
