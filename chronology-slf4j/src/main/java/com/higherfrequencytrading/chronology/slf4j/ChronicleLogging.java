package com.higherfrequencytrading.chronology.slf4j;


import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * @author lburgazzoli
 */
public class ChronicleLogging {

    public static void warmup() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (factory instanceof ChronicleLoggerFactory) {
            ((ChronicleLoggerFactory) factory).warmup();
        }
    }

    public static void shutdown() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (factory instanceof ChronicleLoggerFactory) {
            ((ChronicleLoggerFactory) factory).shutdown();
        }
    }
}
