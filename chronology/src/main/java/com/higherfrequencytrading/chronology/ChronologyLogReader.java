package com.higherfrequencytrading.chronology;

import net.openhft.lang.io.Bytes;

public interface ChronologyLogReader {
    public ChronologyLogEvent read(Bytes bytes);
}
