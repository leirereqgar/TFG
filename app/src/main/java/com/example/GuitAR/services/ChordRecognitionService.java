package com.example.GuitAR.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ChordRecognitionService extends Service {
    static {
        System.loadLibrary("native-lib");
    }
    private final IBinder binder = new ChordRecognitionBinder();
    private final int BUFFER_SIZE = 8192;
    private static int [] chord = new int[2];
    double [] audio_samples_buffer;
    double [] audio_samples_buffer_window;
    double [] audio_spectrum_buffer;
    boolean keep_recording = false;
    private AudioRecord r;

    @Override
    public void onCreate() {
        chord[0] = -1;
        chord[1] = -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        r.stop();
        r.release();
    }

    public int [] getChord() {
        return chord;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int ID){
        audio_samples_buffer        = new double[BUFFER_SIZE];
        audio_samples_buffer_window = new double[BUFFER_SIZE];
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        process();

                    }
                }
        ).start();

        return super.onStartCommand(i, flags, ID);
    }

    @SuppressLint("MissingPermission")
    public void process() {
        short[] temp_samples = new short[BUFFER_SIZE];
        int n_read;
        r = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                44100,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_DEFAULT,
                BUFFER_SIZE);
        r.startRecording();
        keep_recording = true;
        while(keep_recording) {
            r.read(temp_samples, 0, temp_samples.length);
            audio_samples_buffer = getSamplesToDouble(temp_samples);
            audio_samples_buffer_window = window(audio_samples_buffer);
            audio_spectrum_buffer = bandPassFilter(
                    fft(audio_samples_buffer_window, true),
                    55, 4000,44100,8192);
            chord = chordDetection(audio_samples_buffer_window,
                    audio_spectrum_buffer);
        }
    }

    private static double[] getSamplesToDouble(short[] inputBuffer) {
        double[] outputBuffer = new double[inputBuffer.length];
        for (int i = 0; i < inputBuffer.length;  i++) {
            outputBuffer[i] = (double)inputBuffer[i] / (double)Short.MAX_VALUE;
        }
        return outputBuffer;
    }

    /**
     * Metodos para acceder al codigo en C++
     */
    private static native int[] chordDetection(double[] samples, double[] spectrum_samples);
    private static native double[] getChromagram();
    private static native double[] window(double[] samples);
    private static native double[] fft(double[] v, boolean DIRECT);
    private static native double[] bandPassFilter(double[] samples, float low_cut, float high_cut, float sr, float buffer_size);

    public class ChordRecognitionBinder extends Binder {
        public ChordRecognitionService getService() {
            return ChordRecognitionService.this;
        }
    }
}