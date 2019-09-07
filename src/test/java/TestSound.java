import audio.AudioManager;
import audio.Speaker;
import audio.Track;

import java.io.IOException;

public class TestSound {
    public static void main(String[] args) throws IOException {
        AudioManager.run();
        Track sound0 = AudioManager.createTrack("/hollow.ogg");
        Track sound1 = AudioManager.createTrack("/get-outta-here.ogg");
        Speaker speaker = AudioManager.createSpeaker();

        speaker.queueTrack(sound0);
        speaker.queueTrack(sound1);

        try {
            speaker.loop(true);
            speaker.play();

            Thread.sleep(10000);

            speaker.stop();
            speaker.clear();
            speaker.queueTrack(sound0);
            speaker.loop(true);
            speaker.play();

            speaker.setPosition(5, 0, 0);
            Thread.sleep(10000);
            speaker.setPosition(-5, 0, 0);
            Thread.sleep(10000);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            AudioManager.cleanup();
        }
    }
}
