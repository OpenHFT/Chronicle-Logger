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

package net.openhft.chronicle.logger;

import net.openhft.chronicle.Chronicle;
import net.openhft.lang.model.constraints.NotNull;
import net.openhft.lang.model.constraints.Nullable;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

public abstract class ChronicleLogAppenderConfig {

    public abstract Chronicle build(String path)
        throws IOException;

    public void setProperties(final @NotNull Properties properties, final @Nullable String prefix) {
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String name = entry.getKey().toString();
            final String value = entry.getValue().toString();

            if(prefix != null && !prefix.isEmpty()) {
                if (name.startsWith(prefix)) {
                    setProperty(name.substring(prefix.length()), value);
                }
            } else {
                setProperty(name, value);
            }
        }
    }

    public void setProperty(final @NotNull String propName, final @NotNull String propValue) {
        try {
            final PropertyDescriptor property = new PropertyDescriptor(propName, this.getClass());
            final Method method = property.getWriteMethod();
            final Class<?> type = method.getParameterTypes()[0];

            if(type != null && propValue != null && !propValue.isEmpty()) {
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
            throw new RuntimeException(e);
        }
    }
}
