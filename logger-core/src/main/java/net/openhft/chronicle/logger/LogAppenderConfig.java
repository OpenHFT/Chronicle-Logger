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
package net.openhft.chronicle.logger;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.WireType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

public class LogAppenderConfig {

    private static final String[] KEYS = new String[]{
            "blockSize",
            "bufferCapacity",
            "rollCycle",
            "compression"
    };

    private int blockSize;
    private long bufferCapacity;
    private String rollCycle;

    public LogAppenderConfig() {
    }

    // *************************************************************************
    //
    // *************************************************************************

    public int getBlockSize() {
        return this.blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public long getBufferCapacity() {
        return this.bufferCapacity;
    }

    public void setBufferCapacity(long bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public String getRollCycle() {
        return rollCycle;
    }

    public void setRollCycle(String rollCycle) {
        this.rollCycle = rollCycle;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public String[] keys() {
        return KEYS;
    }

    public ChronicleQueue build(String path, String wireType) {
        WireType wireTypeEnum = wireType != null ? WireType.valueOf(wireType.toUpperCase()) : WireType.BINARY_LIGHT;
        SingleChronicleQueueBuilder builder = ChronicleQueue.singleBuilder(path)
                .wireType(wireTypeEnum)
                .blockSize(blockSize)
                .bufferCapacity(bufferCapacity);
        if (rollCycle != null)
            builder.rollCycle(RollCycles.valueOf(rollCycle));
        return builder.build();
    }

    public void setProperties(@NotNull final Properties properties, @Nullable final String prefix) {
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String name = entry.getKey().toString();
            final String value = entry.getValue().toString();

            if (prefix != null && !prefix.isEmpty()) {
                if (name.startsWith(prefix)) {
                    setProperty(name.substring(prefix.length()), value);
                }
            } else {
                setProperty(name, value);
            }
        }
    }

    public void setProperty(@NotNull final String propName, @NotNull final String propValue) {
        try {
            final PropertyDescriptor property = new PropertyDescriptor(propName, this.getClass());
            final Method method = property.getWriteMethod();
            final Class<?> type = method.getParameterTypes()[0];

            if (type != null && propValue != null && !propValue.isEmpty()) {
                if (type == int.class) {
                    method.invoke(this, Integer.parseInt(propValue));

                } else if (type == long.class) {
                    method.invoke(this, Long.parseLong(propValue));

                } else if (type == boolean.class) {
                    method.invoke(this, Boolean.parseBoolean(propValue));

                } else if (type == String.class) {
                    method.invoke(this, propValue);
                }
            }
        } catch (Exception e) {
        }
    }

}
