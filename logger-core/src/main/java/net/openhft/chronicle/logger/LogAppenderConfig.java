package net.openhft.chronicle.logger;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.WireType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogAppenderConfig {

    /** The file name containing configuration */
    public static final String CONFIG_TOML = "config.toml";

    /**
     * The content encoding, similar to Content-Encoding HTTP header.
     *
     * The codec registry is responsible for managing the mapping from name to codec.
     * A content encoding may use dictionary compression, which must be included as
     * a file local to the chronicle queue.
     */
    private String contentEncoding = "identity";

    /**
     * The content type of the data, in MIME type format.
     */
    private String contentType = "application/octet-stream";

    /**
     * The block size of the chronicle queue
     */
    private int blockSize = 128;

    /**
     * The buffer capacity of the chronicle queue.
     */
    private long bufferCapacity = 256;

    /**
     * The roll cycle of the chronicle queue.
     */
    private String rollCycle = RollCycles.DAILY.toString();

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public long getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(long bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public String getRollCycle() {
        return rollCycle;
    }

    public void setRollCycle(String rollCycle) {
        this.rollCycle = rollCycle;
    }

    /**
     * Writes the configuration to a file on the filesystem.
     *
     * @param config the configuration object
     * @param path the filesystem path.
     * @throws IOException if the file cannot be written
     */
    public static void write(LogAppenderConfig config, Path path) {
        try {
            TomlWriter tomlWriter = new TomlWriter();
            Path filePath = Files.isDirectory(path) ? path.resolve("config.toml") : path;
            tomlWriter.write(config, filePath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Reads a config object from the path.
     *
     * @param path the filesystem path.
     * @return the configuration object.
     */
    public static LogAppenderConfig parse(Path path) {
        Path filePath = Files.isDirectory(path) ? path.resolve(CONFIG_TOML) : path;
        Toml toml = new Toml().read(filePath.toFile());
        return toml.to(LogAppenderConfig.class);
    }

    /**
     * Reads a config object from string, using TOML configuration
     */
    public static LogAppenderConfig parse(String tomlString) {
        Toml toml = new Toml().read(tomlString);
        return toml.to(LogAppenderConfig.class);
    }

    public ChronicleQueue build(Path path) {
        SingleChronicleQueueBuilder builder = ChronicleQueue.singleBuilder(path)
                .wireType(WireType.BINARY_LIGHT)
                .blockSize(blockSize)
                .bufferCapacity(bufferCapacity);
        if (!(rollCycle == null || rollCycle.isEmpty()))
            builder.rollCycle(RollCycles.valueOf(rollCycle));

        return builder.build();
    }

    public ChronicleQueue build(String pathString) {
        Path path = Paths.get(pathString);
        return build(path);
    }

}
