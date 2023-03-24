package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class LessonsScreen extends AppCompatActivity {
    private static int n_lessons = 4;
    private ArrayList<Button> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_screen);

        setUpCarousel();

        buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.c1));
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startLesson();
                }
            });
        }
    }

    private void startLesson() {
        Intent change = new Intent(this, LessonActivity.class);
        startActivity(change);
    }

    private void setUpCarousel() {
        Carousel carousel = findViewById(R.id.carousel);
        carousel.setAdapter(new Carousel.Adapter() {
            @Override
            public int count() {
                return n_lessons;
            }

            @Override
            public void populate(View view, int index) {

            }

            @Override
            public void onNewItem(int index) {

            }
        });
    }
}