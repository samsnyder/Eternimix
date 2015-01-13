package uk.ac.cam.ss2249.remix;

/**
 * Provides an interface for recieving progress on track analysis
 *
 * @author Sam Snyder
 */
public interface ProgressFeedback {
    void changedState(TrackLoadState state);
    void gotProgress(double progress);
    void finished(boolean success);
}
