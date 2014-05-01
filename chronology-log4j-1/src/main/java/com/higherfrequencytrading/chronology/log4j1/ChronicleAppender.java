package com.higherfrequencytrading.chronology.log4j1;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.ExcerptAppender;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;

public abstract class ChronicleAppender extends AppenderSkeleton implements Appender {

    private String path;
    private boolean includeCallerData;
    private boolean includeMDC;

    private Chronicle chronicle;
    private ExcerptAppender appender;

    protected ChronicleAppender() {
        this.path = null;
        this.includeCallerData = true;
        this.includeMDC = true;

        this.chronicle = null;
        this.appender = null;
    }

    // *************************************************************************
    // Custom logging options
    // *************************************************************************

    public void setIncludeCallerData(boolean logCallerData) {
        this.includeCallerData = logCallerData;
    }

    public boolean isIncludeCallerData() {
        return this.includeCallerData;
    }

    public void setIncludeMappedDiagnosticContext(boolean logMDC) {
        this.includeMDC = logMDC;
    }

    public boolean isIncludeMappedDiagnosticContext() {
        return this.includeMDC;
    }

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
    protected void append(final LoggingEvent event) {
        createAppender();

        if(this.appender != null) {
            ChronicleAppenderHelper.write(
                this.appender,
                event,
                this.includeMDC,
                this.includeCallerData);
        }
    }

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
}
