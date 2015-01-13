package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A player that plays through a queue of beats
 *
 * @author Sam Snyder
 */
class Player {

    private PlayerDelegate delegate;

    private Queue<Beat> audioQueue;

    private int bufferSize = 8192;
    private int minQueueItems = 5;
    private int addNum = 4;

    private Beat lastAddedBeat;

    /**
     * Creates a player with a delegate
     *
     * @param d delegate
     */
    protected Player(PlayerDelegate d){
        delegate = d;
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
                try {
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, delegate.getAudioFormat(), 1);
                    SourceDataLine soundLine = (SourceDataLine) AudioSystem.getLine(info);
                    soundLine.open(delegate.getAudioFormat(), bufferSize);
                    soundLine.start();
                    while (!audioQueue.isEmpty()) {
                        Beat beat = audioQueue.remove();

                        delegate.willPlayBeat(beat);
                        if(beat.getDecodedAudio() == null){
                            System.out.println("NO AUDIO");
                            continue;
                        }

                        long duration = timeToIndex(beat.getDuration());
                        soundLine.write(beat.getDecodedAudio(), 0, (int) duration);
                        beat.deallocDecodedAudio();

                        if(shouldAddToQueue()){
                            refreshQueue();
                        }
                    }
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private long timeToIndex(double time){
        long frames = Math.round(time * delegate.getAudioFormat().getFrameRate());
        return delegate.getAudioFormat().getFrameSize() * frames;
    }

}
