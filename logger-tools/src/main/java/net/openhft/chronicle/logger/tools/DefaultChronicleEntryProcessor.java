package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.ValueIn;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
import org.slf4j.helpers.MessageFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DefaultChronicleEntryProcessor implements ChronicleEntryProcessor {

    @Override
    public String apply(ChronicleLogEvent e) {
        if (e.contentType == null) {
            // if no content type, then return a UTF-8 string.
            return new String(e.entry, StandardCharsets.UTF_8);
        } else {
            return "UNKNOWN";
        }
    }
}
