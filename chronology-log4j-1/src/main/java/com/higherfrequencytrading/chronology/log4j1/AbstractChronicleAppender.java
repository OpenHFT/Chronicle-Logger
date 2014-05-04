package com.higherfrequencytrading.chronology.log4j1;

import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends AppenderSkeleton implements Appender {

    private String path;

    protected Chronicle chronicle;
    protected ExcerptAppender appender;

    protected AbstractChronicleAppender() {
        this.path = null;
        this.chronicle = null;
        this.appender = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    // *************************************************************************
    // Chronicle implementation
    // *************************************************************************

    protected abstract Chronicle createChronicle() throws IOException;

    protected void createAppender() {
        if(this.chronicle == null) {
            try {
                this.chronicle = createChronicle();
                this.appender  = this.chronicle.createAppender();
            } catch(IOException e) {
                //TODO: manage exception
                this.chronicle = null;
                this.appender  = null;
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void close() {
        if(this.chronicle != null) {
            try {
                if(this.appender != null) {
                    this.appender.close();
                }

                if(this.chronicle != null) {
                    this.chronicle.close();
                }
            } catch(IOException e) {
                //TODO: manage exception
            }
        }
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static int toIntChronologyLogLevel(final Level level) {
        switch(level.toInt()) {
            case Level.DEBUG_INT:
                return ChronologyLogLevel.DEBUG.levelInt;
            case Level.TRACE_INT:
                return ChronologyLogLevel.TRACE.levelInt;
            case Level.INFO_INT:
                return ChronologyLogLevel.INFO.levelInt;
            case Level.WARN_INT:
                return ChronologyLogLevel.WARN.levelInt;
            case Level.ERROR_INT:
                return ChronologyLogLevel.ERROR.levelInt;
            default:
                throw new IllegalArgumentException(level.toInt() + " not a valid level value");
        }
    }

    public static String toStrChronologyLogLevel(final Level level) {
        switch(level.toInt()) {
            case Level.DEBUG_INT:
                return ChronologyLogLevel.DEBUG.levelStr;
            case Level.TRACE_INT:
                return ChronologyLogLevel.TRACE.levelStr;
            case Level.INFO_INT:
                return ChronologyLogLevel.INFO.levelStr;
            case Level.WARN_INT:
                return ChronologyLogLevel.WARN.levelStr;
            case Level.ERROR_INT:
                return ChronologyLogLevel.ERROR.levelStr;
            default:
                throw new IllegalArgumentException(level.toInt() + " not a valid level value");
        }
    }
}
