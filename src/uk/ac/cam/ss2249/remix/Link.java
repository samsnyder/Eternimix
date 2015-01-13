package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 11/01/15.
 */
public class Link {

    private Beat source;
    private Beat destination;
    private double score;

    Link(Beat s, Beat d, double sc){
        source = s;
        destination = d;
        score = sc;
    }

    public Beat getSource() {
        return source;
    }

    public Beat getDestination() {
        return destination;
    }

    public double getScore() {
        return score;
    }
}
