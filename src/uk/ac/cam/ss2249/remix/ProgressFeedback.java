package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 13/01/15.
 */
public interface ProgressFeedback {
    void changedState(TrackLoadState state);
    void gotProgress(double progress);
    void finished(boolean success);
}
