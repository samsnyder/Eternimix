package uk.ac.cam.ss2249.remix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Remixes a single song infinitely
 *
 * @author Sam Snyder
 */
public class SingleTrackRemixer implements PlayerDelegate{

    private Track currentTrack;
    private Player player;
    private List<UI> uis;

    private double curRandomBranchChance = 0;
    private double minRandomBranchChance = 0.18;
    private double maxRandomBranchChance = 0.5;
    private double randomBranchChanceDelta = 0.018;

    /**
     * Creates a remixer that feeds a UI
     *
     * @param audioPlayer audio player
     */
    public SingleTrackRemixer(AudioPlayerInterface audioPlayer){
        player = new Player(this, audioPlayer);
        uis = new ArrayList<UI>();
    }

    /**
     * Adds a UI listener
     *
     * @param ui UI
     */
    public void addUI(UI ui){
        uis.add(ui);
    }

    /**
     * Removes a UI listener
     *
     * @param ui UI
     */
    public void removeUI(UI ui){
        uis.remove(ui);
    }

    /**
     * Loads a track into the remixer
     *
     * @param t track
     */
    public void loadTrack(Track t){
        currentTrack = t;
        for(UI ui : uis)
            ui.loadedTrack(t);
    }

    /**
     * Gets the currently loaded track
     *
     * @return track
     */
    public Track getCurrentTrack(){
        return currentTrack;
    }

    /**
     * Starts playing
     */
    public void play(){
        player.startPlaying();
    }

    /**
     * Start from beat
     *
     * @param beat
     */
    public void startFromBeat(Beat beat){
        player.startFromBeat(beat);
    }

    @Override
    public void willPlayBeat(Beat beat) {
        for(UI ui : uis)
            ui.willPlayBeat(beat);
    }

    @Override
    public PCMAudioFormat getAudioFormat() {
        return currentTrack.getAudioFormat();
    }

    @Override
    public Beat getNextBeat(Beat lastBeat) {
        if(lastBeat == null){
            return currentTrack.getBeats().get(0);
        }else{
            int nextIndex = lastBeat.getIndex() + 1;
            if(nextIndex < 0) {
                return currentTrack.getBeats().get(0);
            }else if(nextIndex >= currentTrack.getBeats().size()){
                System.out.println("ENDED " + lastBeat);
                return null;
            }else{
                return selectRandomNextBeat(currentTrack.getBeats().get(nextIndex));
            }
        }
    }

    private Beat selectRandomNextBeat(Beat seed){
        if(seed.getCurrentLinks().size() == 0){
            return seed;
        }else if(shouldRandomBranch(seed)){
            Link next = seed.getCurrentLinks().remove(0);
            seed.getCurrentLinks().add(next);
            return next.getDestination();
        }else{
            return seed;
        }
    }

    private boolean shouldRandomBranch(Beat beat){
        //if(beat != null) return false;
//        if(beat.getIndex() == currentTrack.getLastBranchPoint()){
//            return true;
//        }

        curRandomBranchChance += randomBranchChanceDelta;
        if(curRandomBranchChance > maxRandomBranchChance){
            curRandomBranchChance = maxRandomBranchChance;
        }
        boolean shouldBranch = new Random().nextDouble() < curRandomBranchChance;
        if(shouldBranch){
            curRandomBranchChance = minRandomBranchChance;
        }
        return shouldBranch;
    }
}
