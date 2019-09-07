package audio;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SEC_OFFSET;

public class Speaker {
    final int pointer;
    private final List<Track> tracks;
    private float fullLength;

    Speaker(int pointer) {
        this.pointer = pointer;
        this.tracks = new ArrayList<>();
        this.fullLength = 0;
    }

    public float getFullLength() {
        return fullLength;
    }

    public Track currentTrack() {
        if (alGetSourcei(pointer, AL_LOOPING) == AL_TRUE) {
            float length = 0;
            for (int i = 0; i < tracks.size(); i++) {
                length += tracks.get(i).getLengthInSeconds();
                if (alGetSourcef(pointer, AL_SEC_OFFSET) < length) return tracks.get(i);
            }
            return null;
        } else {
            final int processed = alGetSourcei(pointer, AL_BUFFERS_PROCESSED);
            if (processed == alGetSourcei(pointer, AL_BUFFERS_QUEUED)) return null;
            return tracks.get(processed);
        }
    }

    public boolean isPlaying() {
        return alGetSourcei(pointer, AL_BUFFERS_PROCESSED) != alGetSourcei(pointer, AL_BUFFERS_QUEUED);
    }

    public void queueTrack(Track track) {
        alSourceQueueBuffers(pointer, track.pointer);

        if (alGetError() == AL_NO_ERROR) {
            ++track.refCount;
            tracks.add(track);
            fullLength += track.getLengthInSeconds();
        }
    }

    public void loop(boolean on) {
        alSourcei(pointer, AL_LOOPING, on ? AL_TRUE : AL_FALSE);
    }

    public void play() {
        alSourcePlay(pointer);
    }

    public void pause() {
        alSourcePause(pointer);
    }

    public void stop() {
        alSourceStop(pointer);
    }

    public void rewind() {
        alSourceRewind(pointer);
    }

    public void setPitch(float pitch) {
        alSourcef(pointer, AL_PITCH, pitch);
    }

    public void setGain(float gain) {
        alSourcef(pointer, AL_GAIN, gain);
    }

    public void setPosition(float x, float y, float z) {
        alSource3f(pointer, AL_POSITION, x, y, z);
    }

    public void setDirection(float x, float y, float z) {
        alSource3f(pointer, AL_DIRECTION, x, y, z);
    }

    public void setVelocity(float x, float y, float z) {
        alSource3f(pointer, AL_VELOCITY, x, y, z);
    }

    public void clear() {
        if (!tracks.isEmpty()) {
            alSourcei(pointer, AL_LOOPING, AL_FALSE);
            alSourceStop(pointer);

            for (int i = 0; i < tracks.size(); i++) {
                alSourceUnqueueBuffers(pointer);
                final Track sound = tracks.get(i);
                --sound.refCount;
            }

            tracks.clear();
            fullLength = 0;
        }
    }
}
