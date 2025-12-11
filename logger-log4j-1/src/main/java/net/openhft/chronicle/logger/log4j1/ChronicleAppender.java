/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.log4j1;

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;

import java.io.IOException;

/**
 * Log4j 1.x appender that writes events to a Chronicle Queue.
 * <p>
 * Configuration is delegated to {@link LogAppenderConfig}.  The setter
 * methods on this class forward any options to that configuration object
 * before the queue is created.  When {@link #activateOptions()} is called the
 * queue is built using the values supplied via these setters.
 */
public final class ChronicleAppender extends AbstractChronicleAppender {

    /**
     * Chronicle configuration driving queue creation.
     */
    private final LogAppenderConfig config;

    /**
     * Creates an appender with default Chronicle configuration.
     */
    public ChronicleAppender() {
        this.config = new LogAppenderConfig();
    }

    // Custom logging options
    /**
     * Set the Chronicle Queue block size in bytes.
     * Call this before {@link #activateOptions()}.
     *
     * @param blockSize size of each block in bytes
     */
    public void setBlockSize(int blockSize) {
        config.setBlockSize(blockSize);
    }

    /**
     * Configure the internal ring buffer size.
     *
     * @param bufferCapacity capacity in bytes
     */
    public void setBufferCapacity(int bufferCapacity) {
        config.setBufferCapacity(bufferCapacity);
    }

    /**
     * Returns the configured roll cycle name.
     *
     * @return roll cycle enum name
     */
    public String rollCycle() {
        return config.getRollCycle();
    }

    /**
     * Select the roll cycle to apply when building the queue.
     * The value must match an entry in {@code RollCycles}.
     *
     * @param rollCycle roll cycle name
     */
    public void rollCycle(String rollCycle) {
        config.setRollCycle(rollCycle);
    }

    /**
     * Builds the Chronicle writer backing this Log4j 1.x appender.
     */
    @Override
    protected ChronicleLogWriter createWriter() {
        return new DefaultChronicleLogWriter(this.config.build(this.getPath(), this.getWireType()));
    }

    // LogAppenderConfig
    LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
