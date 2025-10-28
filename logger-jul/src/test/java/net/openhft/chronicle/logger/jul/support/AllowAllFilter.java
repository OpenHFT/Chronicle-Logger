package net.openhft.chronicle.logger.jul.support;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Simple JUL filter that allows every record through.
 */
public final class AllowAllFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        return true;
    }
}
