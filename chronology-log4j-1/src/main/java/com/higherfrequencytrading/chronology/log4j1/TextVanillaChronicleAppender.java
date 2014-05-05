package com.higherfrequencytrading.chronology.log4j1;

import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.VanillaChronicle;
import net.openhft.chronicle.VanillaChronicleConfig;

import java.io.IOException;

public class TextVanillaChronicleAppender extends TextChronicleAppender {

    private VanillaChronicleConfig config;

    public TextVanillaChronicleAppender() {
        this.config = null;
    }

    public void setConfig(VanillaChronicleConfig config) {
        this.config = config;
    }

    @Override
    protected Chronicle createChronicle() throws IOException {
        return (this.config != null)
            ? new VanillaChronicle(this.getPath(),this.config)
            : new VanillaChronicle(this.getPath());
    }
}
