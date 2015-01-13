package uk.ac.cam.ss2249.remix;

/**
 * Represents a set of float values.
 * Used to represent pitch and timbre frequency spectrums
 *
 * @author Sam Snyder
 */
class ValueSetSegment {
    private float[] values;

    /**
     * Creates a value set
     *
     * @param v value array
     */
    protected ValueSetSegment(float[] v){
        values = v;
    }

    protected double getDistanceFrom(ValueSetSegment seg){
        return euclideanDistance(values, seg.values);
    }

    private double euclideanDistance(float[] a, float[] b){
        float sum = 0;
        for(int i=0; i<a.length; i++){
            float delta = b[i] - a[i];
            sum += delta * delta;
        }
        return Math.sqrt(sum);
    }
}
