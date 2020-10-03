package net.openhft.chronicle.logger.codec.zstd;

import com.github.luben.zstd.ZstdDictTrainer;
import com.github.luben.zstd.ZstdException;
import net.openhft.chronicle.logger.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ZStandardDictionary {

    private static final Logger logger = LoggerFactory.getLogger(ZStandardDictionary.class);

    public static final String DEFAULT_DICT_FILENAME = "dictionary";

    public static Function<ZstdDictTrainer, CompletionStage<byte[]>> writeToFile(Path path) {
        // Run this as a future so it's off the current thread (we don't
        // care when it completes)
        return trainer -> CompletableFuture.supplyAsync(() -> {
            Path filename = Files.isDirectory(path) ? path.resolve(DEFAULT_DICT_FILENAME) : path;
            try {
                logger.info("Training zstd dictionary samples...");
                byte[] dictBytes = trainer.trainSamples();
                logger.info("Writing zstd dictionary of {} bytes to {}", dictBytes.length, filename);
                Files.write(filename, dictBytes, CREATE_NEW);
                return dictBytes;
            } catch (IOException e) {
                throw new CompletionException(new CodecException(e));
            } catch (ZstdException e) {
                // XXX throws ZstdException (should use a try / catch)
                // https://github.com/facebook/zstd/issues/1735
                // I think "Src size is incorrect" means that there's not enough unique info in the
                // messages to create a dictionary.  So keep going?  Or throw out the
                // trainer and start from scratch?
                throw new CompletionException(new CodecException("Cannot create dictionary: probably not enough unique data", e));
            }
        }, ForkJoinPool.commonPool());

    }

    /**
     * Utility for reading bytes from a file.
     *
     * @param path a path to a regular readable file
     * @return the bytes of the file.
     * @throws IOException if the bytes cannot be read.
     */
    public static byte[] readFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            String msg = String.format("Dictionary %s does not exist!", path);
            throw new FileNotFoundException(msg);
        }
        if (Files.isDirectory(path)) {
            Path dictPath = path.resolve(DEFAULT_DICT_FILENAME);
            return Files.readAllBytes(dictPath);
        }
        return Files.readAllBytes(path);
    }
}
