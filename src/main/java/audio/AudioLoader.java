package audio;

import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;

final class AudioLoader {
    private AudioLoader() {
    }

    static ShortBuffer load(String path, STBVorbisInfo info) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final ByteBuffer buffer = ioResourceToByteBuffer(path, 32 * 1024);
            IntBuffer errorBuffer = stack.mallocInt(1);
            long decoder = STBVorbis.stb_vorbis_open_memory(buffer, errorBuffer, null);
            if (decoder == MemoryUtil.NULL)
                throw new RuntimeException("Failed to open ogg file. Error code: " + errorBuffer.get(0));
            STBVorbis.stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            int lengthSamples = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            ShortBuffer encoded = MemoryUtil.memAllocShort(lengthSamples);
            encoded.limit(STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, encoded) * channels);
            return encoded;
        }
    }

    private static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) ;
            }

        } else {
            try (InputStream source = AudioLoader.class.getResourceAsStream(resource); ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
