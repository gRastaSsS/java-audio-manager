package audio;

import static org.lwjgl.openal.AL10.*;

public class Track {
    final int pointer;
    int refCount;
    private final float lengthInSeconds;

    private static float calculateLength(int buffer) {
        float size = alGetBufferi(buffer, AL_SIZE);
        float bits = alGetBufferi(buffer, AL_BITS);
        float channels = alGetBufferi(buffer, AL_CHANNELS);
        float frequency = alGetBufferi(buffer, AL_FREQUENCY);
        if (alGetError() == AL_NO_ERROR) return (size / channels / (bits / 8)) / frequency;
        else return -1;
    }

    Track(int pointer) {
        this.pointer = pointer;
        this.lengthInSeconds = calculateLength(pointer);
        this.refCount = 0;
    }

    public float getLengthInSeconds() {
        return lengthInSeconds;
    }
}
