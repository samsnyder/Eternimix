package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.LineUnavailableException;

/**
 * Created by sam on 13/01/15.
 */
public interface AudioPlayerInterface {
    void openAudio(int bufferSize) throws LineUnavailableException;
    void writeBuffer(byte[] buffer, int frames);
    void closeAudio();
}
