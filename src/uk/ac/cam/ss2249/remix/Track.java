package uk.ac.cam.ss2249.remix;

import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by sam on 11/01/15.
 */
public class Track {

    private String fileName;
    private RandomAccessFile audioFileInput;
    private AudioFormat audioFormat;
    private AnalyseMethod analyseMethod;
    private List<Beat> beats;

    private int maxLinks = 4;
    private double maxLinkScore = 3200;
    private int minLinkSize = 2;

    private double computedThreshold;
    private double currentThreshold;

    public Track(String f){
        fileName = f;
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
        analyseMethod = new VampAnalyseMethod(this);
    }

    protected String getFileName() {
        return fileName;
    }

    public List<Beat> getBeats() {
        return beats;
    }

    public RandomAccessFile getAudioFileInput() {
        return audioFileInput;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void load(ProgressFeedback progress) throws IOException, UnsupportedAudioFileException {
        progress.changedState(TrackLoadState.COVERTING_TRACK);
        convertTrack(progress);
        progress.changedState(TrackLoadState.ANALYSING_TRACK);
        analyseTrack();
        progress.finished(true);
    }

    public void convertTrack(ProgressFeedback progress) throws IOException, UnsupportedAudioFileException {
        analyseMethod.setupAnalyse();

        File file = new File(fileName);

        //long numFrames = AudioSystem.getAudioFileFormat(file).properties().keySet();
        long duration = ((Long) AudioSystem.getAudioFileFormat(file).properties().get("duration")).longValue();

        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        AudioInputStream din = AudioSystem.getAudioInputStream(audioFormat, in);

        File pcmFile = new File("/Users/sam/Downloads/" + Math.abs(new Random().nextLong()) + ".pcm");

        FileOutputStream outputFile = new FileOutputStream(pcmFile);
        int nRead;
        byte[] data = new byte[analyseMethod.getBufferSize()];
        double n = 0;
        while ((nRead = din.read(data, 0, data.length)) != -1) {
            outputFile.write(data, 0, nRead);
            analyseMethod.processData(data);
            int frames = nRead / audioFormat.getFrameSize();
            n += 1000000 * ((float) frames) / audioFormat.getFrameRate();
            progress.gotProgress(n / ((double) duration));
        }
        outputFile.close();
        beats = analyseMethod.getBeats();
        audioFileInput = new RandomAccessFile(pcmFile, "r");
    }

    public void analyseTrack(){

        //minLinkSize = beats.size() / 1;

        for(int i=0; i<beats.size(); i++){
            Beat beat = beats.get(i);
            beat.calculateAllBeatScores();
//          beat.findAllLinks();
        }
        for(int i=0; i<beats.size(); i++){
            Beat beat = beats.get(i);
            beat.findAllLinks(maxLinks, maxLinkScore, minLinkSize);
        }

        computedThreshold = bestThresholdForLinks(beats.size() / 6);
        System.out.println("Threshold is " + computedThreshold);

//        for(Beat b : beats)
//            System.out.println(b);
//
//        System.out.println(Beat.totTimbre / Beat.num);
//        System.out.println(Beat.totPitch / Beat.num);
//        System.out.println(Beat.totMean / Beat.num);
//        System.out.println(Beat.totDur / Beat.num);
    }

    double bestThresholdForLinks(int targetLinks){
        double threshold;
        for(threshold = 40; threshold < maxLinkScore; threshold += 5){
            int linkCount = countAllLinksWithThreshold(threshold);
            if(linkCount >= targetLinks)
                break;
        }
        return threshold;
    }

    int countAllLinksWithThreshold(double threshold){
        int sum = 0;
        for(int i=0; i<beats.size(); i++){
            Beat beat = beats.get(i);
            beat.extractNeighboursWithThreshold(threshold, maxLinks);
            sum += beat.getCurrentLinks().size();
        }
        return sum;
    }

}
