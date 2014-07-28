package com.higherfrequencytrading.chronology.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

@Plugin(
    name        = "BinaryIndexedChronicle",
    category    = "Core",
    elementType = "appender",
    printObject = true)
public class BinaryIndexedChronicleAppender extends BinaryChronicleAppender {

    private ChronicleConfig config;
    private Object lock;
    private ExcerptAppender appender;

    public BinaryIndexedChronicleAppender(String name, Filter filter) {
        super(name,filter);
        this.config = null;
        this.appender = null;
        this.lock = new Object();
    }

    public void setConfig(ChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        Chronicle chronicle = (this.config != null)
            ? new IndexedChronicle(this.getPath(),this.config)
            : new IndexedChronicle(this.getPath());

        this.appender = chronicle.createAppender();

        return chronicle;
    }

    @Override
    protected ExcerptAppender getAppender() {
        return this.appender;
    }

    @Override
    public void append(final LogEvent event) {
        synchronized (this.lock) {
            super.append(event);
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @PluginFactory
    public static BinaryIndexedChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("formatMessage") final String formatMessage,
        @PluginAttribute("includeCallerData") final String includeCallerData,
        @PluginAttribute("includeMappedDiagnosticContext") final String includeMappedDiagnosticContext,
        @PluginElement("filters") final Filter filter) {

        if(name == null) {
            LOGGER.error("No name provided for BinaryVanillaChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for BinaryVanillaChronicleAppender");
            return null;
        }

        BinaryIndexedChronicleAppender appender = new BinaryIndexedChronicleAppender(name, filter);
        appender.setPath(path);

        if(formatMessage != null) {
            appender.setFormatMessage("true".equalsIgnoreCase(formatMessage));
        }

        if(includeCallerData != null) {
            appender.setIncludeCallerData("true".equalsIgnoreCase(includeCallerData));
        }

        if(includeMappedDiagnosticContext != null) {
            appender.setIncludeMappedDiagnosticContext("true".equalsIgnoreCase(includeMappedDiagnosticContext));
        }

        return appender;
    }
}
