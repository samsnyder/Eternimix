package uk.ac.cam.ss2249.remix;

import javax.sound.sampled.*;
import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * Represents a track, made up of multiple beats.
 *
 * @author Sam Snyder
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

    /**
     * Creates a track object with a file path
     *
     * @param f file path
     */
    public Track(String f){
        fileName = f;
        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
        analyseMethod = new VampAnalyseMethod(this);
    }

    /**
     * Gets the tracks original encoded file path
     *
     * @return file path
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets a list of beats that make up the track.
     *
     * @return list of beats
     */
    public List<Beat> getBeats() {
        return beats;
    }

    protected RandomAccessFile getAudioFileInput() {
        return audioFileInput;
    }

    protected AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Loads the track with a progress reporter
     *
     * It first converts and draws the beats, pitches and timbre from the encoded file
     * It saves the decoded PCM data to a temporary file
     *
     * @param progress reporter
     * @throws IOException in reading the encoded file and writing the decoded file
     * @throws UnsupportedAudioFileException in decoding and analysing the audio
     */
    public void load(ProgressFeedback progress) throws IOException, UnsupportedAudioFileException {
        progress.changedState(TrackLoadState.COVERTING_TRACK);
        convertTrack(progress);
        progress.changedState(TrackLoadState.ANALYSING_TRACK);
        analyseTrack();
        progress.finished(true);
    }

    private void convertTrack(ProgressFeedback progress) throws IOException, UnsupportedAudioFileException {
        analyseMethod.setupAnalyse();

        File file = new File(fileName);
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

    private void analyseTrack(){

        for(Beat beat : beats){
            beat.calculateAllBeatScores();
        }
        for(Beat beat : beats){
            beat.findAllLinks(maxLinks, maxLinkScore, minLinkSize);
        }

        currentThreshold = computedThreshold = bestThresholdForLinks(beats.size() / 6);
    }

    private double bestThresholdForLinks(int targetLinks){
        double threshold;
        for(threshold = 40; threshold < maxLinkScore; threshold += 5){
            int linkCount = countAllLinksWithThreshold(threshold);
            if(linkCount >= targetLinks)
                break;
        }
        return threshold;
    }

    private int countAllLinksWithThreshold(double threshold){
        int sum = 0;
        for(int i=0; i<beats.size(); i++){
            Beat beat = beats.get(i);
            beat.extractLinksWithThreshold(threshold);
            sum += beat.getCurrentLinks().size();
        }
        return sum;
    }

    public int changeThreshold(double threshold){
        currentThreshold = threshold;
        return countAllLinksWithThreshold(currentThreshold);
    }

}
