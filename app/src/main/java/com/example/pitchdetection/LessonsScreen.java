package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;

import android.os.Bundle;
import android.view.View;

public class LessonsScreen extends AppCompatActivity {
    private static int n_lessons = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_screen);
    }

//    private void setUpCarousel() {
//        Carousel carousel = findViewById(R.id.carousel);
//        carousel.setAdapter(new Carousel.Adapter() {
//            @Override
//            public int count() {
//                return n_lessons;
//            }
//
//            @Override
//            public void populate(View view, int index) {
//
//            }
//
//            @Override
//            public void onNewItem(int index) {
//
//            }
//        });
//    }
}