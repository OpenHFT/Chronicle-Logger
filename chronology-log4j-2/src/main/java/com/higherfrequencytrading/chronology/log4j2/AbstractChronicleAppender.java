package com.higherfrequencytrading.chronology.log4j2;


import com.higherfrequencytrading.chronology.ChronologyLogLevel;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.IOException;

public abstract class AbstractChronicleAppender extends AbstractAppender {
    private String path;

    protected Chronicle chronicle;
    protected ExcerptAppender appender;

    protected AbstractChronicleAppender(String name, Filter filter) {
        super(name, filter, null, true);

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

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    public void start() {
        if(getPath() == null) {
            LOGGER.error("Appender " + getName() + " has configuration errors and is not started!");
        } else {
            try {
                this.chronicle = createChronicle();
                this.appender  = this.chronicle.createAppender();
            } catch(IOException e) {
                this.chronicle = null;
                this.appender  = null;
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }

            super.start();
        }
    }

    @Override
    public void stop() {
        if(this.chronicle != null) {
            try {
                this.chronicle.close();
            } catch(IOException e) {
                LOGGER.error("Appender " + getName() + " " + e.getMessage());
            }
        }

        super.stop();
    }

    // *************************************************************************
    //
    // *************************************************************************

    public static int toIntChronologyLogLevel(final Level level) {
        if(level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronologyLogLevel.DEBUG.levelInt;
        } else if(level.intLevel() == Level.TRACE.intLevel()) {
            return ChronologyLogLevel.TRACE.levelInt;
        } else if(level.intLevel() == Level.INFO.intLevel()) {
            return ChronologyLogLevel.INFO.levelInt;
        } else if(level.intLevel() == Level.WARN.intLevel()) {
            return ChronologyLogLevel.WARN.levelInt;
        } else if(level.intLevel() == Level.ERROR.intLevel()) {
            return ChronologyLogLevel.ERROR.levelInt;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
    }

    public static String toStrChronologyLogLevel(final Level level) {
        if(level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronologyLogLevel.DEBUG.levelStr;
        } else if(level.intLevel() == Level.TRACE.intLevel()) {
            return ChronologyLogLevel.TRACE.levelStr;
        } else if(level.intLevel() == Level.INFO.intLevel()) {
            return ChronologyLogLevel.INFO.levelStr;
        } else if(level.intLevel() == Level.WARN.intLevel()) {
            return ChronologyLogLevel.WARN.levelStr;
        } else if(level.intLevel() == Level.ERROR.intLevel()) {
            return ChronologyLogLevel.ERROR.levelStr;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
    }
}
