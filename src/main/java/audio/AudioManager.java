package audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import java.io.IOException;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public final class AudioManager {
    public static final Listener LISTENER = new Listener();
    private static long DEVICE, CONTEXT;

    private AudioManager() {
    }

    public static void run() {
        if (DEVICE != MemoryUtil.NULL)
            throw new IllegalStateException("Manager is already running. Cleanup to run again");

        DEVICE = alcOpenDevice(alcGetString(MemoryUtil.NULL, ALC_DEFAULT_DEVICE_SPECIFIER));

        if (DEVICE == MemoryUtil.NULL)
            throw new RuntimeException("No sound device has been found");

        CONTEXT = alcCreateContext(DEVICE, new int[] {0});
        alcMakeContextCurrent(CONTEXT);
        AL.createCapabilities(ALC.createCapabilities(DEVICE));
    }

    public static void cleanup() {
        if (DEVICE == MemoryUtil.NULL)
            throw new IllegalStateException("Already cleaned up");

        alcDestroyContext(CONTEXT);
        alcCloseDevice(DEVICE);
        CONTEXT = MemoryUtil.NULL;
        DEVICE = MemoryUtil.NULL;
        alGetError();
    }

    public static Track createTrack(String path) throws IOException {
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer audioBuffer = AudioLoader.load(path, info);
            int format = info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            int sampleRate = info.sample_rate();

            int bufferPointer = alGenBuffers();
            alBufferData(bufferPointer, format, audioBuffer, sampleRate);
            LibCStdlib.free(audioBuffer);

            return alGetError() == AL_NO_ERROR ? new Track(bufferPointer) : null;
        }
    }

    public static boolean deleteTrack(Track sound) {
        if (sound.refCount == 0) {
            alDeleteBuffers(sound.pointer);
            return alGetError() == AL_NO_ERROR;
        }

        return false;
    }

    public static Speaker createSpeaker() {
        return new Speaker(alGenSources());
    }

    public static boolean deleteSpeaker(Speaker source) {
        source.clear();
        alDeleteSources(source.pointer);
        return alGetError() == AL_NO_ERROR;
    }
}
