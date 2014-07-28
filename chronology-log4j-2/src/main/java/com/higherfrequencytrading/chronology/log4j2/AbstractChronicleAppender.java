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

    protected AbstractChronicleAppender(String name, Filter filter) {
        super(name, filter, null, true);

        this.path = null;
        this.chronicle = null;
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

    protected abstract ExcerptAppender getAppender();

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
            } catch(IOException e) {
                this.chronicle = null;
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

    public static ChronologyLogLevel toChronologyLogLevel(final Level level) {
        if(level.intLevel() == Level.DEBUG.intLevel()) {
            return ChronologyLogLevel.DEBUG;
        } else if(level.intLevel() == Level.TRACE.intLevel()) {
            return ChronologyLogLevel.TRACE;
        } else if(level.intLevel() == Level.INFO.intLevel()) {
            return ChronologyLogLevel.INFO;
        } else if(level.intLevel() == Level.WARN.intLevel()) {
            return ChronologyLogLevel.WARN;
        } else if(level.intLevel() == Level.ERROR.intLevel()) {
            return ChronologyLogLevel.ERROR;
        }

        throw new IllegalArgumentException(level.intLevel() + " not a valid level value");
    }
}
