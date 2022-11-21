package com.example.pitchdetection.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;

import com.example.pitchdetection.JNIParser;
import com.google.android.material.button.MaterialButton;

import enums.Actions;
import enums.Info;

public class ChordDetectionService extends Service {
    ChordReceiver receiver;

    int [] chord_detected = new int[2];
    boolean keep_processing;
    boolean keep_recording;

    double[] audioSamplesBuffer;
    double[] audioSamplesBufferWindowed;
    double[] audioSpectrumBuffer;

    @Override
    public void onCreate() {
        receiver = new ChordReceiver();
        registerChordReceiver();

        chord_detected[0] = -1;
        chord_detected[1] = -1;
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startID) {
        //AudioStack.initAudioStack();
        audioSamplesBuffer = new double[8192];
        audioSpectrumBuffer = new double[8192];

        setActivityChords();

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
    }

    private void sendMessageToActivity(Actions msg, Bundle extras) {
        try{
            Intent i = new Intent();
            i.setAction((msg.toString()));

            if(extras != null)
                i.putExtras(extras);

            sendBroadcast(i);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerChordReceiver(){
        receiver = new ChordReceiver();
        try{
            IntentFilter filter = new IntentFilter();
            for(int i = 0; i < Actions.values().length; i++)
                filter.addAction(Actions.values()[i].toString());

            registerReceiver(receiver, filter);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void processAudio() {
        if(keep_processing) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final int[] detection = JNIParser.chordDetection(audioSamplesBufferWindowed, audioSpectrumBuffer);
                    chord_detected = detection;
                    System.out.println(chord_detected[0]);
                    System.out.println(chord_detected[1]);

                    setActivityChords();
                }
            }).start();
        }
    }

    public void recordAudio() {
        System.out.println("record audio");
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] tempAudioSamples = new short[8192];
                int numberOfShortRead;
                long totalShortsRead = 0;
                @SuppressLint("MissingPermission")
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        44100,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_DEFAULT,
                        8192);

                record.startRecording();

                keep_recording = true;

                while(keep_recording)  {
                    numberOfShortRead = record.read(tempAudioSamples, 0,tempAudioSamples.length);
                    totalShortsRead+=numberOfShortRead;
                    audioSamplesBuffer = JNIParser.getSamplesToDouble(tempAudioSamples);
                    audioSamplesBufferWindowed = JNIParser.window(audioSamplesBuffer);
                    audioSpectrumBuffer = JNIParser.bandPassFilter(JNIParser.fft(audioSamplesBufferWindowed, true), 55, 4000);
                    processAudio();
                }

                record.stop();
                record.release();
            }
        }).start();
    }

    private void setActivityChords() {
        Bundle extras = new Bundle();
        extras.putIntArray(Actions.CHORD_DETECTED.toString(), chord_detected);
        sendMessageToActivity(Actions.DETECTION_DONE, extras);
    }

    public class ChordReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String msg = intent.getAction();
                if(msg != null) {
                    if(msg.equals(Actions.START_RECORDING.toString())) {
                        recordAudio();
                    }
                    else if(msg.equals(Actions.STOP_RECORDING.toString())){
                        keep_recording = false;
                    }
                    else if(msg.equals((Actions.PAUSE_ACTIVITY.toString()))) {
                        keep_recording = false;
                    }
                    else if(msg.equals(Actions.DESTROY_SERVICE.toString())){
                        onDestroy();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
