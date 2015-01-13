package uk.ac.cam.ss2249.remix;

import java.util.List;

/**
 * Abstract class for an analysing method.
 * Strategy design pattern
 *
 * @author Sam Snyder
 */
abstract class AnalyseMethod {

    private Track track;

    protected AnalyseMethod(Track t){
        track = t;
    }

    protected Track getTrack(){
        return track;
    }

    abstract void setupAnalyse();
    abstract void processData(byte[] buffer);
    abstract List<Beat> getBeats();
    abstract int getBufferSize();

}
