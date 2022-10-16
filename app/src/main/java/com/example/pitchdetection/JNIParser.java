package com.example.pitchdetection;

public class JNIParser {
    static {
        System.loadLibrary("native-lib");
    }

    public static double[] window(double[] samples) {
        return windowJni(samples);
    }

    public void init() {

    }

    public static int[] chordDetection(double[] samples) {
        return chordDetectionJni(samples);
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
    private static native int[] chordDetectionJni(double[] samples);
    private static native double[] windowJni(double[] samples);
}
