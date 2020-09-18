package net.openhft.chronicle.logger.codec;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictTrainer;
import com.github.luben.zstd.ZstdException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ZStandardCodecTest {

    @Test
    public void testZStandard() throws IOException {
        // https://github.com/luben/zstd-jni/blob/master/src/test/scala/ZstdDict.scala
        int sampleSize = 1024 * 1024;
        int dictSize = 32 * 1024;
        final AtomicLong dictId = new AtomicLong(-1);
        Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook = trainer -> {
            try {
                byte[] dictBytes = trainer.trainSamples();
                dictId.set(Zstd.getDictIdFromDict(dictBytes));
                return CompletableFuture.completedFuture(dictBytes);
            } catch (ZstdException e) {
                String msg = String.format("Cannot create dictionary with sampleSize %s, dictSize %s", sampleSize, dictSize);
                throw new CodecException(msg, e);
            }
        };
        ZStandardCodec codec = new ZStandardCodec(sampleSize, dictSize, trainingHook);

        String userDir = System.getProperty("user.dir");
        byte[] bytes = Files.readAllBytes(Paths.get(userDir, "src/test/resources/message.json"));

        for (int i = 0; i < 10000; i++) {
            byte[] compress = codec.compress(bytes);
            assertNotNull(compress);
        }

        byte[] withDict = codec.compress(bytes);
        assertEquals(dictId.get(), Zstd.getDictIdFromFrame(withDict));
    }
}
