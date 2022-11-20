package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.pdf.PdfDocument;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.pitchdetection.services.ChordDetectionService;

import enums.Actions;
import enums.ChordTypeEnum;
import enums.NoteNameEnum;

public class ChordDectectionActivity extends AppCompatActivity {
    ChordDetectionReceiver receiver;

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
            }
        });

        initActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();
        startProcessing();
        startRecording();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRecording();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();

        try{
            unregisterReceiver(receiver);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initActivity() {
        Intent service_intent = new Intent(getApplicationContext(), ChordDetectionService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            try {
                getApplicationContext().startForegroundService(service_intent);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else
            getApplicationContext().startService(service_intent);

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
                        Thread.sleep(1000 / 24);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                processFrame();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void startRecording() {
        sendMessageToService(Actions.START_RECORDING);
        keep_recording = true;
    }

    private void stopRecording() {
        sendMessageToService(Actions.STOP_RECORDING);
        keep_recording = false;
    }

    public void processFrame() {
        chord_detected_text.setText(chord_name.fromInteger(chord_detected[0]).toString() + " " +
                                    chord_type.fromInteger(chord_detected[1]).toString());
    }

    private void switchActivity() {
        Intent cambio = new Intent(this, MainActivity.class);
        startActivity(cambio);
    }

    private void sendMessageToService(Actions msg) {
        try {
            Intent i = new Intent();
            i.setAction(msg.toString());
            sendBroadcast(i);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerBroadcastReceiver(){
        receiver = new ChordDetectionReceiver();
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

    class ChordDetectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String msg = intent.getAction();
                Bundle extras = intent.getExtras();
                if(msg != null) {
                    if(msg.equals(Actions.START_RECORDING)) {
                    }
                    else if(msg.equals(Actions.STOP_RECORDING)){
                    }
                    else if(msg.equals(Actions.DETECTION_DONE)){
                        chord_detected = extras.getIntArray(Actions.CHORD_DETECTED.toString());
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}