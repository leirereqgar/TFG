package com.example.GuitAR;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class HomeScreen extends AppCompatActivity {
    private int request_perm_code;
    Button start_btn, help_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        /* Si no se tienen permisos se piden, y si sigen sin estar antes de cerrar la aplicacion
         * se muestra un aviso
         */
        if(!checkPermissions()) {
            requestPermissions();
            if(!checkPermissions()) {
                Toast.makeText(this, "Son necesarios los permisos de camara y microfono",
                        Toast.LENGTH_SHORT);
            }
        }

        start_btn = findViewById(R.id.start);
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToLessons();
            }
        });

        help_btn  = findViewById(R.id.help);
        help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                help();
            }
        });
    }

    /*
    * En las ultimas versiones de android es necesario pedir de forma explicita los permisos
    * en este caso son necesarios los permisos de acceso a la camara y al microfono
    */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(HomeScreen.this,
                new String[]{CAMERA, RECORD_AUDIO}, request_perm_code);
    }

    public boolean checkPermissions() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA)
                == PackageManager.PERMISSION_GRANTED)
                &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED);
    }

    private void goToLessons() {
        Intent change = new Intent(this, LessonSelectionScreen.class);
        startActivity(change);
    }

    private void help() {
        Intent change = new Intent(this, HelpScreen.class);
        startActivity(change);
    }
}