package com.example.pitchdetection.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.pitchdetection.JNIParser;

public class ChordRecognitionService extends Service {
    private final IBinder binder = new ChordRecognitionBinder();
    private final int BUFFER_SIZE = 8192;
    private static int [] chord = new int[2];
    double [] audio_samples_buffer;
    double [] audio_samples_buffer_window;
    double [] audio_spectrum_buffer;
    boolean keep_recording = false;

    @Override
    public void onCreate() {
        chord[0] = -1;
        chord[1] = -1;
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

    public void process() {
        short[] temp_samples = new short[BUFFER_SIZE];
        int n_read;
        @SuppressLint("MissingPermission")
        AudioRecord r = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                                44100,
                                AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_DEFAULT,
                                BUFFER_SIZE);
        r.startRecording();
        keep_recording = true;
        while(keep_recording) {
            n_read = r.read(temp_samples, 0, temp_samples.length);
            audio_samples_buffer = JNIParser.getSamplesToDouble(temp_samples);
            audio_samples_buffer_window = JNIParser.window(audio_samples_buffer);
            audio_spectrum_buffer = JNIParser.bandPassFilter(
                    JNIParser.fft(audio_samples_buffer_window, true),
                    55, 4000);
            chord = JNIParser.chordDetection(audio_samples_buffer_window,
                                    audio_spectrum_buffer);
        }
    }

    public class ChordRecognitionBinder extends Binder {
        public ChordRecognitionService getService() {
            return ChordRecognitionService.this;
        }
    }
}