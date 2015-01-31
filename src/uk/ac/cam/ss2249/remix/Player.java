package uk.ac.cam.ss2249.remix;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A player that plays through a queue of beats
 *
 * @author Sam Snyder
 */
class Player {

    private PlayerDelegate delegate;
    private AudioPlayerInterface audioPlayer;

    private Queue<Beat> audioQueue;

    private int bufferSize = 8192;
    private int minQueueItems = 5;
    private int addNum = 4;

    private Beat lastAddedBeat;

    /**
     * Creates a player with a delegate
     *
     * @param d delegate
     * @param ap audio interface
     */
    protected Player(PlayerDelegate d, AudioPlayerInterface ap){
        delegate = d;
        audioPlayer = ap;
        audioQueue = new LinkedList<Beat>();
    }

    protected boolean shouldAddToQueue(){
        return audioQueue.size() < minQueueItems;
    }

    protected void refreshQueue(){
        for(int i=0; i<addNum; i++){
            addToQueue(delegate.getNextBeat(lastAddedBeat));
        }
    }

    protected void startFromBeat(Beat beat){
        audioQueue.clear();
        addToQueue(beat);
        refreshQueue();
    }

    protected void addToQueue(Beat beat){
        if(beat == null)
            return;
        lastAddedBeat = beat;
        audioQueue.add(beat);
        beat.asyncDecode(timeToIndex(beat.getStart()), timeToIndex(beat.getDuration()));
    }

    protected void startPlaying(){
        if(audioQueue.isEmpty()){
            refreshQueue();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                audioPlayer.openAudio(bufferSize);
                while (!audioQueue.isEmpty()) {
                    Beat beat = audioQueue.remove();

                    delegate.willPlayBeat(beat);
                    if(beat.getDecodedAudio() == null){
                        System.out.println("NO AUDIO");
                        continue;
                    }

                    long duration = timeToIndex(beat.getDuration());
                    audioPlayer.writeBuffer(beat.getDecodedAudio(), (int) duration);
                    if(!audioQueue.contains(beat))
                        beat.deallocDecodedAudio();

                    if(shouldAddToQueue()){
                        refreshQueue();
                    }
                }
            }
        }).start();
    }

    private long timeToIndex(double time){
        long frames = Math.round(time * delegate.getAudioFormat().getFrameRate());
        return delegate.getAudioFormat().getFrameSize() * frames;
    }

}
