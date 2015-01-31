package uk.ac.cam.ss2249.remix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a beat of a song.
 *
 * @author Sam Snyder
 */
public class Beat {

    private Track track;
    private int index;
    private double start;
    private double duration;
    private List<ValueSetSegment> overlappingPitchSegments;
    private List<ValueSetSegment> overlappingTimbreSegments;
    private int indexInBar;

    private byte[] decodedAudio;

    private double[] beatScores;
    private List<Link> allLinks;
    private List<Link> currentLinks;

    private double timbreWeight = 85;
    private double pitchWeight = 3;
    private double durationWeight = 2000;

    /**
     * Creates a new beat with positions in the track
     *
     * @param t the track it belongs to
     * @param i the index in the track
     * @param s the start time in seconds
     * @param d the duration in seconds
     * @param iib the index in the bar
     */
    public Beat(Track t, int i, double s, double d, int iib){
        track = t;
        index = i;
        start = s;
        duration = d;
        indexInBar = iib;
        overlappingPitchSegments = new ArrayList<ValueSetSegment>();
        overlappingTimbreSegments = new ArrayList<ValueSetSegment>();
    }

    /**
     * The track the beat belongs to
     *
     * @return track
     */
    public Track getTrack() {
        return track;
    }

    /**
     * The index in the track
     *
     * @return index
     */
    public int getIndex() {
        return index;
    }

    /**
     * The start time
     *
     * @return start time in seconds
     */
    public double getStart() {
        return start;
    }

    /**
     * The duration
     *
     * @return duration in seconds
     */
    public double getDuration() {
        return duration;
    }

    public double getEnd(){
        return start + duration;
    }

    protected byte[] getDecodedAudio() {
        return decodedAudio;
    }

    /**
     * The links from the beat under the current threshold
     *
     * @return list of links
     */
    public List<Link> getCurrentLinks() {
        return currentLinks;
    }

    public void addOverlappingPitchSegment(ValueSetSegment seg){
        overlappingPitchSegments.add(seg);
    }

    public void addOverlappingTimbreSegment(ValueSetSegment seg){
        overlappingTimbreSegments.add(seg);
    }

    public void setOverlappingTimbreSegments(List<ValueSetSegment> timbre){
        overlappingTimbreSegments = timbre;
    }

    protected void extractLinksWithThreshold(double maxScore){
        currentLinks = new ArrayList<Link>();
        for(int i=0; i<allLinks.size(); i++){
            Link link = allLinks.get(i);
            if(link.getScore() < maxScore)
                currentLinks.add(link);
        }
    }


    protected void calculateAllBeatScores(){
        beatScores = new double[track.getBeats().size()];
        for(int i=0; i<track.getBeats().size(); i++){
            if(index == i){
                beatScores[index] = Double.MAX_VALUE;
                continue;
            }

            beatScores[i] = getScoreFromBeat(track.getBeats().get(i));
        }
    }

    protected double getScoreFromBeatWithSurround(Beat dest){
        int checkSize = 1;
        double score = 0;
        for(int j=-checkSize; j<=checkSize; j++){
            if(index <= checkSize || index >= track.getBeats().size()-checkSize-1 || dest.getIndex() <= checkSize || dest.getIndex() >= track.getBeats().size()-checkSize-1) {
                score += 2000;
                continue;
            }
            Beat thisSource = track.getBeats().get(index + j);
            score += thisSource.scoreToBeatAtIndex(dest.getIndex() + j) * Math.abs(1 + checkSize - j);
        }
        return score;
    }

    protected double scoreToBeatAtIndex(int i){
        return beatScores[i];
    }

    protected void findAllLinks(int maxLinks, double maxScore, int minLinkSize){
        List<Link> links = new ArrayList<Link>();
        for(int i=0; i<track.getBeats().size(); i++){
            if(Math.abs(i - index) < 1)
                continue;
            Beat beat = track.getBeats().get(i);
            double score = getScoreFromBeatWithSurround(beat);
            if(score < maxScore){
                Link link = new Link(this, beat, score);
                links.add(link);
            }
        }

        Collections.sort(links, new Comparator<Link>() {
            @Override
            public int compare(Link a, Link b) {
                if(a.getScore() == b.getScore()) return 0;
                else if(a.getScore() < b.getScore()) return -1;
                else return 1;
            }
        });

        allLinks = new ArrayList<Link>(maxLinks);
        for(int i=0; i<links.size() && i<maxLinks; i++){
            allLinks.add(links.get(i));
        }
    }

    protected double getScoreFromBeat(Beat b){
        double score = 0;

        //score += pitchWeight * averageValueSetScore(overlappingPitchSegments, b.overlappingPitchSegments);
        score += timbreWeight * averageValueSetScore(overlappingTimbreSegments, b.overlappingTimbreSegments);

        if(indexInBar != b.indexInBar)
            score += 100;
        score += durationWeight*Math.abs(duration - b.duration);

        return score;
    }

    private double averageValueSetScore(List<ValueSetSegment> a, List<ValueSetSegment> b){
        double sum = 0;
        int n = 0;
        for(ValueSetSegment aS : a){
            for(ValueSetSegment bS : b){
                double score = aS.getDistanceFrom(bS);
                sum += score;
                n++;
            }
        }
//        for(int i=0; i<a.size(); i++){
//            double score = 100;
//            if(i < b.size()){
//                score = a.get(i).getDistanceFrom(b.get(i));
//            }
//            sum += score;
//            n++;
//        }
        return sum / n;
    }

    private double averageValueSetScore2(List<ValueSetSegment> a, List<ValueSetSegment> b){
        double sum = 0;
        for(int i=0; i<a.size(); i++){
            double score = 100;
            if(i < b.size()){
                score = a.get(i).getDistanceFrom(b.get(i));
            }
            sum += score;
        }
        return sum / a.size();
    }

    protected void deallocDecodedAudio(){
        decodedAudio = null;
    }

    protected void asyncDecode(final long startIndex, final long duration){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    decodedAudio = new byte[(int) duration];
                    track.getAudioFileInput().seek(startIndex);
                    track.getAudioFileInput().read(decodedAudio, 0, (int) duration);
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }).run();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Beat ");
        sb.append(index);
        sb.append(", start: ");
        sb.append(start);
        sb.append(", duration: ");
        sb.append(duration);
        if(currentLinks != null){
            sb.append(", current links: ");
            sb.append(currentLinks.size());
        }
        return sb.toString();
    }

}