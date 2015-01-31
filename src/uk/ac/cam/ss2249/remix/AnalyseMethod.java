package uk.ac.cam.ss2249.remix;

import java.util.List;

/**
 * Abstract class for an analysing method.
 * Strategy design pattern
 *
 * @author Sam Snyder
 */
public abstract class AnalyseMethod {

    private Track track;

    protected AnalyseMethod(Track t){
        track = t;
    }

    protected Track getTrack(){
        return track;
    }

    public abstract void setupAnalyse();
    public abstract void processData(byte[] buffer);
    public abstract List<Beat> getBeats();

}
