package com.higherfrequencytrading.chronology.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleConfig;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

@Plugin(
    name        = "TextIndexedChronicle",
    category    = "Core",
    elementType = "appender",
    printObject = true)
public class TextIndexedChronicleAppender extends TextChronicleAppender {

    private ChronicleConfig config;
    private Object lock;

    public TextIndexedChronicleAppender(String name, Filter filter) {
        super(name,filter);

        this.config = null;
        this.lock = new Object();
    }

    public void setConfig(ChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return (this.config != null)
            ? new IndexedChronicle(this.getPath(),this.config)
            : new IndexedChronicle(this.getPath());
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
    public static TextIndexedChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("dateFormat") final String dateFormat,
        @PluginAttribute("stackTradeDepth") final String stackTradeDepth,
        @PluginElement("filters") final Filter filter) {

        if(name == null) {
            LOGGER.error("No name provided for TextIndexedChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for TextIndexedChronicleAppender");
            return null;
        }

        TextIndexedChronicleAppender appender = new TextIndexedChronicleAppender(name, filter);
        appender.setPath(path);

        if(dateFormat != null) {
            appender.setDateFormat(dateFormat);
        }

        if(stackTradeDepth != null) {
            appender.setStackTradeDepth(Integer.parseInt(stackTradeDepth));
        }

        return appender;
    }
}
