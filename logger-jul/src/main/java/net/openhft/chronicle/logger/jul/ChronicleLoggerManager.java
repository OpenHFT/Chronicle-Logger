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
package net.openhft.chronicle.logger.jul;

import net.openhft.chronicle.logger.ChronicleLogManager;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ChronicleLoggerManager extends LogManager {

    private final Map<String, Logger> loggers;
    private final ChronicleLogManager manager;

    public ChronicleLoggerManager() {
        this.loggers = new ConcurrentHashMap<>();
        this.manager = ChronicleLogManager.getInstance();
    }

    @Override
    public boolean addLogger(final Logger logger) {
        return false;
    }

    @Override
    public Logger getLogger(final String name) {
        return null;
    }
    @Override
    public Enumeration<String> getLoggerNames() {
        return null;
    }

    // *************************************************************************
    //
    // *************************************************************************
}
