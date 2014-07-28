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

package net.openhft.chronicle.logger.slf4j;


import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.logger.ChronicleLogLevel;

import java.io.Closeable;

/**
 *
 */
public interface ChronicleLogAppender extends Closeable {
    /**
     * @return
     */
    public Chronicle getChronicle();

    /**
     * @param level
     * @param name
     * @param message
     * @param arg1
     */
    public void log(ChronicleLogLevel level, String name, String message, Object arg1);

    /**
     * @param level
     * @param name
     * @param message
     * @param arg1
     * @param arg2
     */
    public void log(ChronicleLogLevel level, String name, String message, Object arg1, Object arg2);

    /**
     * @param level
     * @param name
     * @param message
     * @param args
     */
    public void log(ChronicleLogLevel level, String name, String message, Object... args);

    /**
     * @param level
     * @param name
     * @param message
     * @param throwable
     */
    public void log(ChronicleLogLevel level, String name, String message, Throwable throwable);
}
