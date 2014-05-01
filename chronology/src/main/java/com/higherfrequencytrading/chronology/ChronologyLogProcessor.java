package com.higherfrequencytrading.chronology;

public interface ChronologyLogProcessor {
    public void process(final String message);
    public void process(final ChronologyLogEvent event);
}
