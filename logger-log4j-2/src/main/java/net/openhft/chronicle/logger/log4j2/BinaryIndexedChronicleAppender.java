/*
 * Copyright 2014 Higher Frequency Trading
 *
 * http://www.higherfrequencytrading.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.logger.log4j2;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ChronicleQueueBuilder;
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
    name        = "BinaryIndexedChronicle",
    category    = "Core",
    elementType = "appender",
    printObject = true)
public class BinaryIndexedChronicleAppender extends BinaryChronicleAppender {

    private final IndexedLogAppenderConfig config;
    private final Object lock;
    private ExcerptAppender appender;

    public BinaryIndexedChronicleAppender(
        final String name, final Filter filter, final String path, final IndexedLogAppenderConfig config) {
        super(name, filter, path);
        this.config = config;
        this.appender = null;
        this.lock = new Object();
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        Chronicle chronicle = (this.config != null)
            ? this.config.build(this.getPath())
            : ChronicleQueueBuilder.indexed(this.getPath()).build();

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
    public static BinaryIndexedChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("formatMessage") final String formatMessage,
        @PluginAttribute("includeCallerData") final String includeCallerData,
        @PluginAttribute("includeMappedDiagnosticContext") final String includeMappedDiagnosticContext,
        @PluginElement("indexedChronicleConfig") final IndexedChronicleCfg chronicleConfig,
        @PluginElement("filter") final Filter filter) {
        if(name == null) {
            LOGGER.error("No name provided for BinaryVanillaChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for BinaryVanillaChronicleAppender");
            return null;
        }

        final BinaryIndexedChronicleAppender appender =
            new BinaryIndexedChronicleAppender(name, filter, path, chronicleConfig);

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
