package uk.ac.cam.ss2249.remix;

/**
 * Base class for a UI system.
 *
 * This is fed into the remixer for UI reporting
 *
 * @author Sam Snyder
 */
public interface UI {

    /**
     * The player is about to play this beat
     *
     * @param beat beat to play
     */
    void willPlayBeat(Beat beat);

    /**
     * The player will travel down this link on the next beat
     *
     * @param link link to travel down
     */
    void willTravelDownLink(Link link);

    /**
     * Loaded this track
     *
     * @param track loaded track
     */
    void loadedTrack(Track track);
}
