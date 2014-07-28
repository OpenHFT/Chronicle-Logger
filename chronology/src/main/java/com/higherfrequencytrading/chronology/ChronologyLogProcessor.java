package com.higherfrequencytrading.chronology;

public interface ChronologyLogProcessor {
    public void process(final ChronologyLogEvent event);
}
