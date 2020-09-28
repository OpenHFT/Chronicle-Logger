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

import net.openhft.chronicle.logger.ChronicleLogWriter;
import net.openhft.chronicle.logger.DefaultChronicleLogWriter;
import net.openhft.chronicle.logger.LogAppenderConfig;
import net.openhft.chronicle.logger.codec.CodecRegistry;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.time.Instant;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                             final ChronicleCfg config,
                             final String contentType,
                             final String contentEncoding) {
        super(name, filter, layout, ignoreExceptions, properties, path, wireType, contentType, contentEncoding);

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
            @PluginAttribute("contentType") final String contentType,
            @PluginAttribute("contentEncoding") final String contentEncoding,
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
        return new ChronicleAppender(name, filter, layout, ignoreExceptions, properties, path, wireType, chronicleConfig, contentType, contentEncoding);
    }

    @Override
    public void doAppend(@NotNull final LogEvent event, @NotNull final ChronicleLogWriter writer) {
        Layout<? extends Serializable> layout = getLayout();
        if (layout == null) {
            throw new IllegalStateException("Null layout");
        }

        // XXX Calling encode directly to a reusable bytes might be more efficient
        // on the other hand, it looks like encode just calls toByteArray under the
        // hood anyway.
        // layout.encode(event, byteBufferDestination);
        byte[] entry = layout.toByteArray(event);

        Instant instant = event.getInstant();
        int level = event.getLevel().intLevel();
        writer.write(
                instant.getEpochSecond(),
                instant.getNanoOfSecond(),
                level,
                event.getLoggerName(),
                event.getThreadName(),
                entry,
                getContentType(),
                getContentEncoding()
        );
    }

    @Override
    protected ChronicleLogWriter createWriter() {
        ChronicleQueue cq = config.build(getPath());
        Path parent = Paths.get(cq.fileAbsolutePath()).getParent();
        CodecRegistry registry = CodecRegistry.builder().withDefaults(parent).build();
        return new DefaultChronicleLogWriter(registry, cq);
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected LogAppenderConfig getChronicleConfig() {
        return this.config;
    }
}
