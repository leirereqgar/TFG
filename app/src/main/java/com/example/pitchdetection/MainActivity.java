package com.example.pitchdetection;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

public class MainActivity extends AppCompatActivity {
    private int RequestPermissionCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(!checkPermission()) {
            requestPermission();
            if(!checkPermission()) {
                Toast.makeText(this, "Se necesita permiso para usar el micro", Toast.LENGTH_SHORT);
            }
        }

        // Creamos un AudioDispatcher asociado al micrófono por defecto, obtenido en tiempo de ejecución por
        // la llamada AudioSystem.getTargetDataLine(format)
        //
        // El micrófono seleccionado depe soportar el formato de la frecuencia de muestreo, 16 bits mono
        // y signed big endian solicitado
        // Parámetros:
        //    sampleRate      - La frecuencia de muestreo solicitada debe ser soportada por el micrófono.
        //                      Las frecuencias no estándar pueden dar problemas.
        //    audioBufferSize - El tamaño del buffer indica cuántas muestras se pueden procesar a la vez.
        //                      Es común usar 1024 o 2048.
        //    bufferOverlap   - Cuánto se solapan los buffers, suele ser la mitad.
        // Devuelve:
        //    Un audio dispatcher conectado al micrófono por defecto.
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);


        dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                    AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView t = findViewById(R.id.pitch);
                        t.setText("" + pitchInHz);
                        processPitch(pitchInHz);
                    }
                });

            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    public boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public void processPitch(float pitchInHz) {
        TextView noteText = findViewById(R.id.note);
        String noteName = GuitarNotes.getInstance().getNoteName(pitchInHz);
        noteText.setText(noteName);

        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}