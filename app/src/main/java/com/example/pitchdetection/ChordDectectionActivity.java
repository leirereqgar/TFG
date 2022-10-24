package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.TextView;

import enums.Notas;

public class ChordDectectionActivity extends AppCompatActivity {

    TextView chord_detected_text;
    Notas chord_name = Notas.A_SHARP;
    int [] chord_detected = new int[2];
    boolean keep_processing;
    private PdfDocument.Page canvas;

    double[] audioSamplesBuffer;
    double[] audioSamplesBufferWindowed;
    double[] audioSpectrumBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord_dectection);

        checkCaptureAudioPermission();


        initActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startProcessing();
    }

    public void initActivity() {
        chord_detected[0] = -1;
        chord_detected[1] = -1;
        chord_detected_text = findViewById(R.id.chord);
    }

    public void checkCaptureAudioPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0); //Check the requestCode later
        }
    }

    private void startProcessing() {
        keep_processing = true;
        new Thread(new Runnable(){
            @Override
            public void run() {
                while(keep_processing) {
                    try {
                        Thread.sleep(1000 / 12);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processFrame();
                                processAudio();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void processFrame() {
        chord_detected_text.setText(chord_name.fromInteger(chord_detected[0]).toString());
    }

    public void processAudio() {
        if(keep_processing) {
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
                    numberOfShortRead = record.read(tempAudioSamples, 0,tempAudioSamples.length);
                    totalShortsRead+=numberOfShortRead;
                    audioSamplesBuffer = JNIParser.getSamplesToDouble(tempAudioSamples);
                    audioSamplesBufferWindowed = JNIParser.window(audioSamplesBuffer);
                    audioSpectrumBuffer = JNIParser.bandPassFilter(JNIParser.fft(audioSamplesBufferWindowed, true), 55, 4000);
                    final int[] detection = JNIParser.chordDetection(audioSamplesBufferWindowed, audioSpectrumBuffer);
                    chord_detected = detection;
                    System.out.println(chord_detected[0]);
                    System.out.println(chord_detected[1]);
                }
            }).start();
        }
    }


}