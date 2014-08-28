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
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.logger.VanillaLogAppenderConfig;
import net.openhft.chronicle.logger.log4j2.config.VanillaChronicleCfg;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.IOException;

@Plugin(
    name        = "BinaryVanillaChronicle",
    category    = "Core",
    elementType = "appender",
    printObject = true)
public class BinaryVanillaChronicleAppender extends BinaryChronicleAppender {

    private final VanillaLogAppenderConfig config;

    public BinaryVanillaChronicleAppender(
        final String name, final Filter filter, final String path, final VanillaLogAppenderConfig config) {
        super(name, filter, path);
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return (this.config != null)
            ? new VanillaChronicle(this.getPath(), this.config.cfg())
            : new VanillaChronicle(this.getPath());
    }

    @Override
    protected ExcerptAppender getAppender() {
        try {
            return this.chronicle.createAppender();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected VanillaLogAppenderConfig getChronicleConfig() {
        return this.config;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @PluginFactory
    public static BinaryVanillaChronicleAppender createAppender(
        @PluginAttribute("name") final String name,
        @PluginAttribute("path") final String path,
        @PluginAttribute("formatMessage") final String formatMessage,
        @PluginAttribute("includeCallerData") final String includeCallerData,
        @PluginAttribute("includeMappedDiagnosticContext") final String includeMappedDiagnosticContext,
        @PluginElement("vanillaChronicleConfig") final VanillaChronicleCfg chronicleConfig,
        @PluginElement("filter") final Filter filter) {

        if(name == null) {
            LOGGER.error("No name provided for BinaryVanillaChronicleAppender");
            return null;
        }

        if(path == null) {
            LOGGER.error("No path provided for BinaryVanillaChronicleAppender");
            return null;
        }

        final BinaryVanillaChronicleAppender appender =
            new BinaryVanillaChronicleAppender(name, filter, path, chronicleConfig);

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
