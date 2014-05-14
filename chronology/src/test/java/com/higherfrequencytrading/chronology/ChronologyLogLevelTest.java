package com.higherfrequencytrading.chronology;

import org.junit.Test;

import static com.higherfrequencytrading.chronology.ChronologyLogLevel.fastEqualsIgnoreCase;
import static org.junit.Assert.*;


public class ChronologyLogLevelTest {

    @Test
    public void fastEqualsIgnoreCaseTest() {
        assertTrue(fastEqualsIgnoreCase("ERROR", new String("ERROR")));
        assertTrue(fastEqualsIgnoreCase("ERROR", "error"));
        assertTrue(fastEqualsIgnoreCase("ERROR", "eRrOr"));

        assertFalse(fastEqualsIgnoreCase("ERROR", "ERRO"));
        assertFalse(fastEqualsIgnoreCase("ERROR", "ERRORR"));
        assertFalse(fastEqualsIgnoreCase("ERROR", "ERRAR"));
    }
}
