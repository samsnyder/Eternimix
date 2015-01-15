package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 15/01/15.
 */
public class PCMAudioFormat {
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    private int frameSize;
    private float frameRate;
    private boolean bigEndian;

    PCMAudioFormat(float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian){
        this.sampleRate = sampleRate;
        this.sampleSizeInBits = sampleSizeInBits;
        this.channels = channels;
        this.frameSize = frameSize;
        this.frameRate = frameRate;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }
}
