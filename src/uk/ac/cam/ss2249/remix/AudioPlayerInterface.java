package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 13/01/15.
 */
public interface AudioPlayerInterface {
    void openAudio(int bufferSize);
    void writeBuffer(byte[] buffer, int frames);
    void closeAudio();
    void setFormat(PCMAudioFormat pcmAudioFormat);
}
