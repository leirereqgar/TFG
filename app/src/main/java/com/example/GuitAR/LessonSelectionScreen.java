package com.example.GuitAR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.core.util.Pair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.GuitAR.enums.LessonName;

import java.util.ArrayList;


public class LessonSelectionScreen extends AppCompatActivity {
    ArrayList<Pair<String, LessonName>> lessons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_selection_screen);

        lessons = new ArrayList<>();
        lessons.add(new Pair<>("Clase 1\nAcordes mayores", LessonName.Major));
        lessons.add(new Pair<>("Clase 2\nAcordes menores", LessonName.Minor));
        lessons.add(new Pair<>("Clase 3\nAcordes de 7ª dominante", LessonName.Dominant));
        lessons.add(new Pair<>("Clase 4\nAcordes Shake It Off\nTaylor Swift", LessonName.ShakeItOff));
        lessons.add(new Pair<>("Clase 5\nAcordes I Gotta feelin\nBlack Eyes Peas", LessonName.IGottaFeelin));
        lessons.add(new Pair<>("Clase 6\nAcordes Zombie\nThe Cranberries", LessonName.Zombie));
        lessons.add(new Pair<>("Clase 7\nAcordes Accidentaly In Love\nCounting Crows", LessonName.AccidentalyInLove));

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
                return lessons.size();
            }

            @Override
            public void populate(View view, int index) {
                SharedPreferences sh = getPreferences(Context.MODE_PRIVATE);
                String status = sh.getString(lessons.get(index).first, "");
                ((Button)view).setText(lessons.get(index).first + "\n\n" + status);

                view.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        startLesson(lessons.get(index).second.toString());
                    }
                });
            }

            @Override
            public void onNewItem(int index) {

            }
        });
    }
}