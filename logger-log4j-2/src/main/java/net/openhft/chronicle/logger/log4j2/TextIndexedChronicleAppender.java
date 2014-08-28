/*
 * Copyright 2014 Higher Frequency Trading
 * <p/>
 * http://www.higherfrequencytrading.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.chronicle.logger.IndexedLogAppenderConfig;
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

    private final IndexedLogAppenderConfig config;
    private final Object lock;
    private ExcerptAppender appender;

    public TextIndexedChronicleAppender(
        final String name, final Filter filter, final String path, final IndexedLogAppenderConfig config) {
        super(name, filter, path);

        this.config = null;
        this.appender = null;
        this.lock = new Object();
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        Chronicle chronicle = (this.config != null)
            ? new IndexedChronicle(this.getPath(), this.config.config())
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

    protected IndexedLogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @PluginFactory
    public static TextIndexedChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("dateFormat") final String dateFormat,
        @PluginAttribute("stackTraceDepth") final String stackTraceDepth,
        @PluginElement("filters") final Filter filter) {

        if(name == null) {
            LOGGER.error("No name provided for TextIndexedChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for TextIndexedChronicleAppender");
            return null;
        }

        final TextIndexedChronicleAppender appender =
            new TextIndexedChronicleAppender(name, filter, path, null);

        if(dateFormat != null) {
            appender.setDateFormat(dateFormat);
        }

        if(stackTraceDepth != null) {
            appender.setStackTraceDepth(Integer.parseInt(stackTraceDepth));
        }

        return appender;
    }
}
