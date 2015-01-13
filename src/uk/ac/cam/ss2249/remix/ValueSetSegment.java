package uk.ac.cam.ss2249.remix;

/**
 * Created by sam on 11/01/15.
 */
public class ValueSetSegment {
    private float[] values;
    private double start;
    private double duration;

    ValueSetSegment(double s, double d, float[] v){
        values = v;
        start = s;
        duration = d;
    }

    double getDistanceFrom(ValueSetSegment seg){
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

    @Override
    public String toString(){
        return start + " " + duration;
    }
}
