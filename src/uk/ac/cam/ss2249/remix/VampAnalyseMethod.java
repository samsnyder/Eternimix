package uk.ac.cam.ss2249.remix;

import org.vamp_plugins.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An analysing method using the Vamp audio plugins
 *
 * http://www.vamp-plugins.org
 *
 * @author Sam Snyder
 */
public class VampAnalyseMethod extends AnalyseMethod {

    private PluginLoader loader;

    private String[] pluginKeys = new String[]{"qm-vamp-plugins:qm-barbeattracker", "qm-vamp-plugins:qm-constantq", "qm-vamp-plugins:qm-mfcc"};
    private String[] outputKeys = new String[]{"beats", "constantq", "coefficients"};

    private int blockSize = 1024;

    private Plugin[] plugins;
    private int[] outputNumbers;
    private Object[] totalFeatures;
    private int block = 0;
    private float rate;

    /**
     * Creates an analyse method from a track
     *
     * @param t track
     */
    protected VampAnalyseMethod(Track t){
        super(t);
    }

    @Override
    void setupAnalyse() {
        loader = PluginLoader.getInstance();

        rate = getTrack().getAudioFormat().getFrameRate();
        int channels = getTrack().getAudioFormat().getChannels();

        plugins = new Plugin[pluginKeys.length];
        outputNumbers = new int[pluginKeys.length];
        for(int i=0; i<pluginKeys.length; i++){
            try {
                Object[] d = generatePlugin(loader, pluginKeys[i], outputKeys[i], rate, channels, blockSize);
                plugins[i] = (Plugin) d[0];
                outputNumbers[i] = ((Integer) d[1]).intValue();
            } catch (PluginLoader.LoadFailedException e) {
                e.printStackTrace();
            }
        }

        totalFeatures = new Object[plugins.length];
        for(int i=0; i<plugins.length; i++)
            totalFeatures[i] = new LinkedList<Feature>();
    }

    @Override
    void processData(byte[] raw) {
        float[][] buffer = convertRawToFloatArray(raw);
        RealTime timestamp = RealTime.frame2RealTime(block * blockSize, (int)(rate + 0.5));
        for(int i=0; i<plugins.length; i++){
            Map<Integer, List<Feature>> features = plugins[i].process(buffer, timestamp);
            if(features.containsKey(outputNumbers[i])){
                ((List<Feature>) totalFeatures[i]).addAll(features.get(outputNumbers[i]));
            }
        }
        block++;
        timestamp.dispose();
    }

    private float[][] convertRawToFloatArray(byte[] raw){
        // 16-bit LE signed PCM only
        int channels = getTrack().getAudioFormat().getChannels();
        int frames = raw.length / (channels * 2);
        float[][] buffers = new float[channels][blockSize];
        for (int i = 0; i < frames; ++i) {
            for (int c = 0; c < channels; ++c) {
                int ix = i * channels + c;
                int ival = (raw[ix*2] & 0xff) | (raw[ix*2 + 1] << 8);
                float fval = ival / 32768.0f;
                buffers[c][i] = fval;
            }
        }
        return buffers;
    }

    @Override
    List<Beat> getBeats() {
        for(int i=0; i<plugins.length; i++){
            Map<Integer, List<Feature>> features = plugins[i].getRemainingFeatures();
            if(features.containsKey(outputNumbers[i])){
                ((List<Feature>) totalFeatures[i]).addAll(features.get(outputNumbers[i]));
            }
        }

        List<Beat> beats = new ArrayList<Beat>();
        double lastTimestamp = -1;
        int lastIndexInBar = -1;
        for(int i=0; i<((List<Feature>) totalFeatures[0]).size(); i++){
            Feature feature = ((List<Feature>) totalFeatures[0]).get(i);
            if(!feature.hasTimestamp)
                continue;
            double timestamp = timestampToDouble(feature.timestamp);
            if(lastTimestamp > 0){
                double duration = timestamp - lastTimestamp;
                Beat beat = new Beat(getTrack(), i-1, lastTimestamp, duration, lastIndexInBar);
                beats.add(beat);
            }
            lastTimestamp = timestamp;
            lastIndexInBar = Integer.parseInt(feature.label);
        }

        Beat currentBeat = beats.get(0);
        lastTimestamp = -1;
        for(int i=0; i<((List<Feature>) totalFeatures[1]).size(); i++){
            Feature feature = ((List<Feature>) totalFeatures[1]).get(i);
            if(!feature.hasTimestamp)
                continue;
            double timestamp = timestampToDouble(feature.timestamp);
            if(lastTimestamp > currentBeat.getEnd() && currentBeat.getIndex() < beats.size()-1){
                currentBeat = beats.get(currentBeat.getIndex() + 1);
            }
            if(lastTimestamp > 0){
                ValueSetSegment segment = new ValueSetSegment(feature.values);
                currentBeat.addOverlappingPitchSegment(segment);
            }
            lastTimestamp = timestamp;
        }

        currentBeat = beats.get(0);
        lastTimestamp = -1;
        for(int i=0; i<((List<Feature>) totalFeatures[2]).size(); i++){
            Feature feature = ((List<Feature>) totalFeatures[2]).get(i);
            if(!feature.hasTimestamp)
                continue;
            double timestamp = timestampToDouble(feature.timestamp);
            if(lastTimestamp > currentBeat.getEnd() && currentBeat.getIndex() < beats.size()-1){
                currentBeat = beats.get(currentBeat.getIndex() + 1);
            }
            if(lastTimestamp > 0){
                ValueSetSegment segment = new ValueSetSegment(feature.values);
                currentBeat.addOverlappingTimbreSegment(segment);
            }
            lastTimestamp = timestamp;
        }

        for(int i=0; i<plugins.length; i++)
            plugins[i].dispose();

        return beats;
    }

    @Override
    int getBufferSize() {
        return blockSize * getTrack().getAudioFormat().getChannels() * 2;
    }

    private static double timestampToDouble(RealTime timestamp){
        return timestamp.sec() + (((double) timestamp.msec())/1000);
    }

    private static Object[] generatePlugin(PluginLoader loader, String key, String outputKey, float rate, int channels, int blockSize) throws PluginLoader.LoadFailedException {
        Plugin p = loader.loadPlugin(key, rate, PluginLoader.AdapterFlags.ADAPT_ALL);

        OutputDescriptor[] outputs = p.getOutputDescriptors();
        int outputNumber = -1;
        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i].identifier.equals(outputKey)) outputNumber = i;
        }
        if (outputNumber < 0) {
            System.err.println("Plugin lacks output id: " + outputKey);
            System.err.print("Outputs are:");
            for (int i = 0; i < outputs.length; ++i) {
                System.err.print(" " + outputs[i].identifier);
            }
            System.err.println("");
            return null;
        }

        boolean b = p.initialise(channels, blockSize, blockSize);
        if (!b) {
            System.err.println("Plugin initialise failed");
            return null;
        }

        return new Object[]{p, new Integer(outputNumber)};
    }



}
