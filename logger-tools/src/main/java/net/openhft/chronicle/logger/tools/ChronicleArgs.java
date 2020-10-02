package net.openhft.chronicle.logger.tools;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.WireType;

public class ChronicleArgs {

    public static ChronicleQueue createChronicleQueue(String[] args) {
        if (args.length >= 1) {
            int i = 0;
            final WireType wt = WireType.BINARY_LIGHT;
            return ChronicleQueue.singleBuilder(args[i].trim()).wireType(wt).build();
        } else {
            throw new IllegalStateException("Not enough arguments!");
        }
    }

}
