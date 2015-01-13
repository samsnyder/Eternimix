package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.AudioFormat;
import java.util.Random;

/**
 * Created by sam on 11/01/15.
 */
public class SingleTrackRemixer implements PlayerDelegate{

    private Track currentTrack;
    private Player player;
    private UI ui;

    private double curRandomBranchChance = 0;
    private double minRandomBranchChance = 0.18;
    private double maxRandomBranchChance = 0.5;
    private double randomBranchChanceDelta = 0.018;

    public SingleTrackRemixer(UI ui){
        this.ui = ui;
        player = new Player(this);
    }

    public void loadTrack(Track t){
        currentTrack = t;
        ui.loadedTrack(t);
    }

    public void play(){
        player.startPlaying();
    }

    @Override
    public void willPlayBeat(Beat beat) {
        ui.willPlayBeat(beat);
    }

    @Override
    public AudioFormat getAudioFormat() {
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

    Beat selectRandomNextBeat(Beat seed){
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

    boolean shouldRandomBranch(Beat beat){
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
