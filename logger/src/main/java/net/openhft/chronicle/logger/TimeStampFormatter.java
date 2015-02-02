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

import net.openhft.lang.io.ByteStringAppender;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeStampFormatter {

    /**
     * Date format -> formatter cache is needed to prevent ThreadLocalMap instances pollution
     * if date format of log appender reassigned in a loop somewhere.
     *
     * Didn't want to add a dependency to, for example, Guava for this 30-line cache
     * with weak values. But maybe it's worth to.
     */

    private static final ReferenceQueue<TimeStampFormatter> refQueue =
        new ReferenceQueue<TimeStampFormatter>();

    private static final Map<String, FormatterReference> formatters =
        new HashMap<String, FormatterReference>();

    private static final FieldPosition unusedFieldPosition = new FieldPosition(0);

    private final MutablesCache mutablesCache;

    // *************************************************************************
    //
    // *************************************************************************

    private static class FormatterReference extends WeakReference<TimeStampFormatter> {
        private final String dateFormat;
        public FormatterReference(String dateFormat, TimeStampFormatter formatter) {
            super(formatter, refQueue);
            this.dateFormat = dateFormat;
        }
    }

    public static synchronized TimeStampFormatter fromDateFormat(String dateFormat) {
        FormatterReference ref = formatters.get(dateFormat);
        TimeStampFormatter formatter;
        if (ref == null || (formatter = ref.get()) == null) {
            formatter = new TimeStampFormatter(dateFormat);
            formatters.put(dateFormat, new FormatterReference(dateFormat, formatter));
        }
        expungeStaleEntriesFromFormatters();
        return formatter;
    }

    private static void expungeStaleEntriesFromFormatters() {
        for (Reference<? extends TimeStampFormatter> r; (r = refQueue.poll()) != null;) {
            FormatterReference staleRef = (FormatterReference) r;
            FormatterReference removedRef = formatters.remove(staleRef.dateFormat);
            if (removedRef != staleRef) { // reference was displaced while it was in queue
                formatters.put(removedRef.dateFormat, removedRef);
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    /**
     * Three separate ThreadLocals for sb, format and date demand three ThreadLocalMap.get calls
     * in format() method, that is rather slow.
     */
    private static class Mutables {
        final StringBuffer sb = new StringBuffer();
        final DateFormat format;
        final Date date = new Date();

        private Mutables(String dateFormat) {
            format = new SimpleDateFormat(dateFormat);
        }
    }

    private static class MutablesCache extends ThreadLocal<Mutables> {
        private final String dateFormat;

        private MutablesCache(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        protected Mutables initialValue() {
            return new Mutables(dateFormat);
        }
    }

    private TimeStampFormatter(String dateFormat) {
        mutablesCache = new MutablesCache(dateFormat);
    }

    public void format(long timeStamp, ByteStringAppender appender) {
        Mutables ms = mutablesCache.get();
        StringBuffer sb = ms.sb;
        sb.setLength(0);
        Date date = ms.date;
        date.setTime(timeStamp);
        ms.format.format(date, sb, unusedFieldPosition);
        appender.append(sb);
    }

    public String format(long timeStamp) {
        Mutables ms = mutablesCache.get();
        StringBuffer sb = ms.sb;
        sb.setLength(0);
        Date date = ms.date;
        date.setTime(timeStamp);
        ms.format.format(date, sb, unusedFieldPosition);

        return sb.toString();
    }
}
