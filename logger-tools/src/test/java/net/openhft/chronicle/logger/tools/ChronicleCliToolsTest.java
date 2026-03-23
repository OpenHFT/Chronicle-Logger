/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.core.io.IOTools;
import net.openhft.chronicle.logger.ChronicleLogLevel;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.WireType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLI regression tests for {@link ChroniCat} and {@link ChroniTail}.
 */
public class ChronicleCliToolsTest {

    @Test
    public void chroniCatPrintsUsageWhenNoArgumentsProvided() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            ChroniCat.main(new String[0]);
        } finally {
            System.setErr(originalErr);
        }
        String output = err.toString("UTF-8");
        assertTrue(output.contains("Usage: ChroniCat"));
    }

    @Test
    public void chroniCatPrintsRecordsFromQueue() throws IOException {
        Path queuePath = Files.createTempDirectory("chroni-cat");
        try {
            try (ChronicleQueue queue = ChronicleQueue.singleBuilder(queuePath).wireType(WireType.BINARY_LIGHT).build()) {
                ChronicleLogWriter writer = new DefaultChronicleLogWriter(queue);
                writer.write(
                        ChronicleLogLevel.INFO,
                        System.currentTimeMillis(),
                        "cli-thread",
                        "cli",
                        "test event {} {}",
                        null,
                        "lhs",
                        "rhs");
                writer.close();
            }

            PrintStream originalOut = System.out;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            System.setOut(new PrintStream(out));
            try {
                ChroniCat.main(new String[]{"-w", "binary_light", queuePath.toString()});
            } finally {
                System.setOut(originalOut);
            }
            String output = out.toString("UTF-8");
            assertTrue(output.contains("test event lhs rhs"), "expected formatted message in ChroniCat output");
        } finally {
            IOTools.deleteDirWithFiles(queuePath.toString());
        }
    }

    @Test
    public void chroniCatPrintsStackTraceWhenWireTypeMissing() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            ChroniCat.main(new String[]{"-w"});
        } finally {
            System.setErr(originalErr);
        }
        String output = err.toString("UTF-8");
        assertTrue(output.contains("ArrayIndexOutOfBoundsException"));
    }

    @Test
    public void chroniTailPrintsUsageWhenNoArgumentsProvided() throws UnsupportedEncodingException {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            ChroniTail.main(new String[0]);
        } finally {
            System.setErr(originalErr);
        }
        String output = err.toString("UTF-8");
        assertTrue(output.contains("Usage: ChroniTail"));
    }
}
