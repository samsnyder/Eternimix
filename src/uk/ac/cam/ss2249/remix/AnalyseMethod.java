package uk.ac.cam.ss2249.remix;

import java.util.List;

/**
 * Created by sam on 11/01/15.
 */
abstract class AnalyseMethod {

    private Track track;

    AnalyseMethod(Track t){
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
