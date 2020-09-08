/*
 * Copyright 2014-2017 Chronicle Software
 *
 * http://www.chronicle.software
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

import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

@Plugin(
        name = "Chronicle",
        category = "Core",
        elementType = "appender",
        printObject = true)
public class ChronicleAppender extends AbstractChronicleAppender {

    private final ChronicleCfg config;

    public ChronicleAppender(final String name,
                             final Filter filter,
                             final Layout<? extends Serializable> layout,
                             final boolean ignoreExceptions,
                             final Property[] properties,
                             final String path,
                             final String wireType,
                             final ChronicleCfg config) {
        super(name, filter, layout, ignoreExceptions, properties, path, wireType);

        this.config = config != null ? config : new ChronicleCfg();
    }

    // *************************************************************************
    //
    // *************************************************************************

    @PluginFactory
    public static ChronicleAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginAttribute("path") final String path,
            @PluginAttribute("wireType") final String wireType,
            @PluginElement("layout") final Layout<? extends Serializable> layout,
            @PluginElement("chronicleCfg") final ChronicleCfg chronicleConfig,
            @PluginElement("filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for ChronicleAppender");
            return null;
        }

        if (path == null) {
            LOGGER.error("No path provided for ChronicleAppender");
            return null;
        }

        boolean ignoreExceptions = true;
        Property[] properties = {};
        return new ChronicleAppender(name, filter, layout, ignoreExceptions, properties, path, wireType, chronicleConfig);
    }

    @Override
    public void doAppend(@NotNull final LogEvent event, @NotNull final ChronicleLogWriter writer) {
        Instant instant = Instant.ofEpochMilli(event.getTimeMillis()).plusNanos(event.getNanoTime());
        byte[] bytes = getLayout().toByteArray(event);
        int level = event.getLevel().intLevel();
        writer.write(
                instant,
                level,
                event.getThreadName(),
                event.getLoggerName(),
                BytesStore.wrap(bytes),
                null,
                null
        );
    }

    @Override
    protected ChronicleLogWriter createWriter() throws IOException {
        return new DefaultChronicleLogWriter(config.build(getPath(), getWireType()));
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
