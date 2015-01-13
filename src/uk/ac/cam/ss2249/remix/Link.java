package uk.ac.cam.ss2249.remix;

/**
 * Represents a link between two similar beats
 * Holds a source beat and a destination beat and a score of similarity
 * where lower is better
 *
 * @author Sam Snyder
 */
public class Link {

    private Beat source;
    private Beat destination;
    private double score;

    /**
     * Creates a link with a source, destination and score
     *
     * @param s source
     * @param d destination
     * @param sc score
     */
    protected Link(Beat s, Beat d, double sc){
        source = s;
        destination = d;
        score = sc;
    }

    /**
     * The links source
     *
     * @return source beat
     */
    public Beat getSource() {
        return source;
    }

    /**
     * The links destination
     *
     * @return destination beat
     */
    public Beat getDestination() {
        return destination;
    }

    /**
     * The links score
     *
     * @return score
     */
    public double getScore() {
        return score;
    }
}
