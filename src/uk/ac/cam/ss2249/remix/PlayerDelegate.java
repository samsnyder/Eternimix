package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.AudioFormat;

/**
 * Created by sam on 11/01/15.
 */
public interface PlayerDelegate {
    void willPlayBeat(Beat beat);
    AudioFormat getAudioFormat();
    Beat getNextBeat(Beat lastBeat);
}
