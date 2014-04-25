package com.higherfrequencytrading.chronology.logback;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogbackVanillaChronicleTest extends ChronicleTestBase {

    // *************************************************************************
    //
    // *************************************************************************

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Test
    public void testAppenderSetup() {
        Logger l = LoggerFactory.getLogger("vanilla-chronicle");
        l.debug("test");
    }
}
