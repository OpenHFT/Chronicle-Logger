package com.higherfrequencytrading.chronology.slf4j.impl;


import net.openhft.lang.io.Bytes;

import java.util.Date;

/**
 *
 */
public abstract class AbstractBinaryChronicleLogReader extends AbstractChronicleLogReader {
    @Override
    public void read(Bytes bytes) {
        Date ts = new Date(bytes.readLong());
        int level = bytes.readByte();
        long tid = bytes.readLong();
        String tname = bytes.readEnum(String.class);
        String name = bytes.readEnum(String.class);
        String msg = bytes.readEnum(String.class);
        int nbargs = bytes.readInt();
        Object[] args = nbargs > 0 ? new Object[nbargs] : ChronicleLogWriters.NULL_ARGS;

        for (int i = 0; i < nbargs; i++) {
            args[i] = bytes.readObject();
        }

        this.process(ts, level, tid, tname, name, msg, args);
    }

    @Override
    public void process(String message) {
        throw new UnsupportedOperationException();
    }
}
