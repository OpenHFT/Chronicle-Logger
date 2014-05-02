package com.higherfrequencytrading.chronology.logback;

import net.openhft.lang.io.IOTools;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Ignore
public class LogbackVanillaChroniclePerfTest extends LogbackTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        IOTools.deleteDir(rootPath());
    }

    // *************************************************************************
    // BINARY
    // *************************************************************************

    @Test
    public void testSingleThreadLogging() throws IOException {
        Thread.currentThread().setName("perf-plain-vanilla");

        final Logger clogger   = LoggerFactory.getLogger("perf-binary-vanilla-chronicle");
        final Logger plogger   = LoggerFactory.getLogger("perf-plain-vanilla");
        final long   items     = 10000;

        for(int s=64; s <= 1024 ;s += 64) {
            final String staticStr = StringUtils.leftPad("", s, 'X');

            long cStart1 = System.nanoTime();

            for (int i = 1; i <= items; i++) {
                clogger.info(staticStr);
            }

            long cEnd1 = System.nanoTime();

            long pStart1 = System.nanoTime();

            for (int i = 1; i <= items; i++) {
                plogger.info(staticStr);
            }

            long pEnd1 = System.nanoTime();

            System.out.printf("items=%03d size=%04d=> chronology=%.0f, plain=%.0f\n",
                items,
                staticStr.length(),
                (cEnd1 - cStart1) / items / 1e3,
                (pEnd1 - pStart1) / items / 1e3);
        }
    }
}
