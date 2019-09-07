package audio;

import static org.lwjgl.openal.AL10.*;

public final class Listener {
    Listener() {
    }

    public void setGain(float gain) {
        alListenerf(AL_GAIN, gain);
    }

    public void setPosition(float x, float y, float z) {
        alListener3f(AL_POSITION, x, y, z);
    }

    public void setVelocity(float x, float y, float z) {
        alListener3f(AL_VELOCITY, x, y, z);
    }

    public void setOrientation(float[] atUp) {
        alListenerfv(AL_ORIENTATION, atUp);
    }
}
