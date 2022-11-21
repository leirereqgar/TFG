package com.example.pitchdetection;

public class JNIParser {
    static {
        System.loadLibrary("native-lib");
    }

    public static int[] chordDetection(double[] samples, double[] spectrum_samples) {
        return chordDetectionJNI(samples, spectrum_samples);
    }

    public static double[] window(double[] samples) {
        return windowJNI(samples);
    }

    public static double[] getChromagram(){return getChromagramJNI();}

    public static double[] fft(double[] v, boolean DIRECT){return fftJNI(v, DIRECT);}

    public static double[] bandPassFilter(double[] samples, float low_cut, float high_cut) {
        return bandPasFilterJNI(samples, low_cut, high_cut, 44100, 8192);
    }

    public static double[] getSamplesToDouble(short[] inputBuffer) {
        double[] outputBuffer = new double[inputBuffer.length];
        for (int i = 0; i < inputBuffer.length;  i++) {
            outputBuffer[i] = (double)inputBuffer[i] / (double)Short.MAX_VALUE;
        }
        return outputBuffer;
    }

    /**
     * Metodos para acceder al codigo en C++
     */
    private static native int[] chordDetectionJNI(double[] samples, double[] spectrum_samples);
    private static native double[] getChromagramJNI();
    private static native double[] windowJNI(double[] samples);
    private static native double[] fftJNI(double[] v, boolean DIRECT);
    private static native double[] bandPasFilterJNI(double[] samples, float low_cut, float high_cut, float sr, float buffer_size);
}
