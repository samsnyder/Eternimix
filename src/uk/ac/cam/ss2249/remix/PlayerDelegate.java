package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.AudioFormat;

/**
 * The delegate for the Player
 * This is where the player recieves beats needed for the queue
 * and gives out data when beats are played
 *
 * @author Sam Snyder
 */
interface PlayerDelegate {
    void willPlayBeat(Beat beat);
    PCMAudioFormat getAudioFormat();
    Beat getNextBeat(Beat lastBeat);
}
