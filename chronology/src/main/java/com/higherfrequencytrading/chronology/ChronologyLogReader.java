package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;

public interface ChronologyLogReader {
    public void read(final Bytes bytes);
}
