package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import enums.ChordTypeEnum;
import enums.NoteNameEnum;

public class ChordDectectionActivity extends AppCompatActivity {

    TextView chord_detected_text;
    NoteNameEnum chord_name = NoteNameEnum.A_SHARP;
    ChordTypeEnum chord_type = ChordTypeEnum.NoChord;
    int [] chord_detected = new int[2];
    boolean keep_processing;
    boolean keep_recording;
    private PdfDocument.Page canvas;
    private Button boton;

    double[] audioSamplesBuffer;
    double[] audioSamplesBufferWindowed;
    double[] audioSpectrumBuffer;

    private Thread procesar, record_audio, process_audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chord_dectection);

        checkCaptureAudioPermission();

        boton = findViewById(R.id.button2);
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchActivity();
                keep_recording = false;
                keep_processing = false;
            }
        });

        initActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startProcessing();
    }

    @Override
    protected void onPause() {
        super.onPause();
        keep_processing = false;
        keep_recording = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        keep_processing = true;
        keep_recording = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        keep_processing = true;
        keep_recording = true;
        procesar.interrupt();
        record_audio.interrupt();
        process_audio.interrupt();
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
        procesar = new Thread(new Runnable(){
            @Override
            public void run() {
                while(keep_processing) {
                    try {
                        Thread.sleep(1000 / 24);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processFrame();
                                recordAudio();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        procesar.start();
    }

    public void processFrame() {
        chord_detected_text.setText(chord_name.fromInteger(chord_detected[0]).toString() + " " +
                                    chord_type.fromInteger(chord_detected[1]).toString());
    }

    public void recordAudio() {
        record_audio = new Thread(new Runnable() {
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
        });

        record_audio.start();
    }

    public void processAudio() {
        if(keep_processing) {
            process_audio = new Thread(new Runnable() {
                @Override
                public void run() {
                    final int[] detection = JNIParser.chordDetection(audioSamplesBufferWindowed, audioSpectrumBuffer);
                    chord_detected = detection;
                    System.out.println(chord_detected[0]);
                    System.out.println(chord_detected[1]);
                }
            });
            process_audio.start();
        }
    }

    private void switchActivity() {
        Intent cambio = new Intent(this, MainActivity.class);
        startActivity(cambio);
    }


}