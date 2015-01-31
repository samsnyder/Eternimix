package uk.ac.cam.ss2249.remix;

/**
 * Provides an interface for recieving progress on track analysis
 *
 * @author Sam Snyder
 */
public interface ProgressFeedback {
    void changedState(Track track, TrackLoadState state);
    void gotProgress(Track track, double progress);
    void finished(Track track, boolean success);
    void gotError(Track track, Exception e);
}
