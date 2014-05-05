package com.higherfrequencytrading.chronology.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.VanillaChronicleConfig;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

@Plugin(
    name        = "TextVanillaChronicle",
    category    = "Core",
    elementType = "appender",
    printObject = true)
public class TextVanillaChronicleAppender extends TextChronicleAppender {

    private VanillaChronicleConfig config;

    public TextVanillaChronicleAppender(String name, Filter filter) {
        super(name,filter);

        this.config = null;
    }

    public void setConfig(VanillaChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return (this.config != null)
            ? new VanillaChronicle(this.getPath(),this.config)
            : new VanillaChronicle(this.getPath());
    }

    // *************************************************************************
    //
    // *************************************************************************

    @PluginFactory
    public static TextVanillaChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("dateFormat") final String dateFormat,
        @PluginAttribute("stackTradeDepth") final String stackTradeDepth,
        @PluginElement("filters") final Filter filter) {

        if(name == null) {
            LOGGER.error("No name provided for TextVanillaChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for TextVanillaChronicleAppender");
            return null;
        }

        TextVanillaChronicleAppender appender = new TextVanillaChronicleAppender(name, filter);
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
