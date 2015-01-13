package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 12/01/15.
 */
public abstract class UI {
    private SingleTrackRemixer e;

    protected UI(){
        e = new SingleTrackRemixer(this);
    }

    protected SingleTrackRemixer getEngine(){
        return e;
    }

    abstract protected void willPlayBeat(Beat beat);
    abstract protected void willTravelDownLink(Link link);
    abstract protected void loadedTrack(Track track);
}
