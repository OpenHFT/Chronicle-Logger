/*
 * Copyright 2014-2020 chronicle.software
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
package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.logger.ChronicleLogLevel;
import org.jetbrains.annotations.Nullable;

public interface ChronicleLogProcessor {
    void process(
            final long timestamp,
            final ChronicleLogLevel level,
            final String loggerName,
            final String threadName,
            final String message,
            @Nullable final Throwable throwable,
            final Object[] args);
}
