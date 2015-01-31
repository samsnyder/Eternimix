package uk.ac.cam.ss2249.remix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sam on 15/01/15.
 */
public interface AudioDecoderInterface {
    void openFile(Track track, String fileName) throws IOException;
    byte[] getBuffer() throws IOException;
    void closeFile() throws IOException;
    double getProgress();
    RandomAccessFile getPCMFile() throws FileNotFoundException;
}
