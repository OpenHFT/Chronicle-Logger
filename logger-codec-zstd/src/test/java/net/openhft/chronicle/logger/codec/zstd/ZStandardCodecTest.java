package net.openhft.chronicle.logger.codec.zstd;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictTrainer;
import com.github.luben.zstd.ZstdException;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.logger.codec.CodecException;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ZStandardCodecTest {

    @Test
    public void testBytesWithCodec() throws IOException {
        // https://github.com/luben/zstd-jni/blob/master/src/test/scala/Zstd.scala
        String userDir = System.getProperty("user.dir");
        byte[] bytes = Files.readAllBytes(Paths.get(userDir, "src/test/resources/message.json"));
        int sampleSize = 1024 * 1024;
        int dictSize = 32 * 1024;
        Function<ZstdDictTrainer, CompletionStage<byte[]>> trainingHook = zstdDictTrainer -> null;
        ZStandardCodec codec = new ZStandardCodec(sampleSize, dictSize, 3, trainingHook);

        int size = bytes.length;
        Bytes<ByteBuffer> inputBytes = Bytes.elasticByteBuffer(size);
        ByteBuffer inputBuffer = inputBytes.underlyingObject();
        inputBuffer.put(bytes);
        inputBuffer.flip();
        Bytes<ByteBuffer> compressedBytes = Bytes.elasticByteBuffer((int) Zstd.compressBound(size));
        ByteBuffer compressedBuffer = compressedBytes.underlyingObject();
        codec.compress(inputBuffer, compressedBuffer);
        compressedBuffer.flip();

        long l = Zstd.decompressedSize(compressedBuffer);
        assertEquals(size, l);
        Bytes<ByteBuffer> decompressedBytes = Bytes.elasticByteBuffer(size);
        ByteBuffer decompressedBuffer = decompressedBytes.underlyingObject();
        int decompressedSize = codec.decompress(compressedBuffer, decompressedBuffer);
        assert(decompressedSize == bytes.length);

        inputBuffer.rewind();
        compressedBuffer.rewind();
        decompressedBuffer.flip();

        int comparison = inputBuffer.compareTo(decompressedBuffer);
        boolean result = comparison == 0 && Zstd.decompressedSize(compressedBuffer) == decompressedSize;
        assertTrue(result);
    }

    @Test
    public void testDictionary() throws IOException {
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
        ZStandardCodec codec = new ZStandardCodec(sampleSize, dictSize, 3, trainingHook);

        Bytes<ByteBuffer> sourceBytes = Bytes.elasticByteBuffer();
        Bytes<ByteBuffer> destBytes = Bytes.elasticByteBuffer();

        // Run through enough that we get dictionary compression.
        for (int i = 0; i < sampleSize; i++) {
            try {
                byte[] bytes = ("Message " + i + " at " + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
                sourceBytes.write(bytes);
                ByteBuffer src = sourceBytes.underlyingObject();
                src.position(0);
                src.limit((int) sourceBytes.readLimit());
                destBytes.ensureCapacity(Zstd.compressBound(sourceBytes.readLimit()));
                ByteBuffer dst = destBytes.underlyingObject();
                int actual = codec.compress(src, dst);
                destBytes.readLimit(actual);
                dst.clear();
            } finally {
                sourceBytes.clear();
            }
        }

        // Check we have a dictionary at the end of this.
        assertFalse(dictId.get() == -1);

        destBytes.clear();
        destBytes.ensureCapacity(Zstd.compressBound(sourceBytes.readLimit()));
        int actual = codec.compress(sourceBytes.underlyingObject(), destBytes.underlyingObject());
        destBytes.readPosition(0);
        destBytes.readLimit(actual);

        assertEquals(dictId.get(), Zstd.getDictIdFromFrame(destBytes.toByteArray()));
    }
}
