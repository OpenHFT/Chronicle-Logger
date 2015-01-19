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

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;

import java.io.IOException;
import java.util.logging.ChronicleHandlerConfig;
import java.util.logging.Handler;
import java.util.logging.Level;

public abstract class AbstractChronicleHandler extends Handler {

    private String path;
    private Chronicle chronicle;

    protected AbstractChronicleHandler() {
        this.path = null;
        this.chronicle = null;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected String getPath() {
        return this.path;
    }

    protected Chronicle getChronicle() {
        return this.chronicle;
    }

    protected void configure(ChronicleHandlerConfig cfg) throws IOException {
        this.path = cfg.getString("path", null);

        setLevel(cfg.getLevel("level", Level.ALL));
        setFilter(cfg.getFilter("filter", null));

        this.chronicle = createChronicle();
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;
    protected abstract ExcerptAppender getAppender();
}
