package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.ByteStringAppender;

import java.lang.ref.*;
import java.text.*;
import java.util.*;


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

    private static class FormatterReference extends WeakReference<TimeStampFormatter> {
        private final String dateFormat;
        public FormatterReference(String dateFormat, TimeStampFormatter formatter) {
            super(formatter, refQueue);
            this.dateFormat = dateFormat;
        }
    }

    private static final Map<String, FormatterReference> formatters =
            new HashMap<String, FormatterReference>();

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

    // *********************************************************************
    //
    // *********************************************************************

    private static final FieldPosition unusedFieldPosition = new FieldPosition(0);

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

    private final MutablesCache mutablesCache;

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
}
