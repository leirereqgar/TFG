package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.core.util.Pair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.pitchdetection.enums.LessonName;

import java.util.ArrayList;


public class LessonSelectionScreen extends AppCompatActivity {
    ArrayList<Pair<String, LessonName>> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_selection_screen);

        names = new ArrayList<>();
        names.add(new Pair<>("Clase 1\nAcordes mayores", LessonName.Major));
        names.add(new Pair<>("Clase 2\nAcordes menores", LessonName.Minor));
        names.add(new Pair<>("Clase 3\nAcordes de 7ª dominante", LessonName.Dominant));
        names.add(new Pair<>("Clase 4\nProgresion I-IV-V en C", LessonName.Progression145_C));
        names.add(new Pair<>("Clase 5\nProgresion I-VI-IV-V en C", LessonName.Progression1645_C));
        names.add(new Pair<>("Clase 6\nProgresion I-V-I-IV en C", LessonName.Progression1514_C));
        names.add(new Pair<>("Clase 7\nProgresion I-IV-V en E", LessonName.Progression145_E));

        setUpCarousel();
    }

    private void startLesson(String extra) {
        Intent change = new Intent(this, LessonActivity.class);
        change.putExtra("lesson", extra);
        startActivity(change);
    }

    private void setUpCarousel() {
        Carousel carousel = findViewById(R.id.carousel);
        carousel.setAdapter(new Carousel.Adapter() {
            @Override
            public int count() {
                return names.size();
            }

            @Override
            public void populate(View view, int index) {
                SharedPreferences sh = getPreferences(Context.MODE_PRIVATE);
                String status = sh.getString(names.get(index).first, "");
                ((Button)view).setText(names.get(index).first);

                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        startLesson(names.get(index).second.toString());
                    }
                });
            }

            @Override
            public void onNewItem(int index) {

            }
        });
    }
}