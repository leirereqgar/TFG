package com.example.pitchdetection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LessonSelectionScreen extends AppCompatActivity {
    private static int n_lessons;
    private ArrayList<TextView> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons_selection_screen);

        
        buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.c1));
        buttons.get(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLesson("Major");
            }
        });

        buttons.add(findViewById(R.id.c2));
        buttons.get(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLesson("Minor");
            }
        });

        buttons.add(findViewById(R.id.c3));
        buttons.get(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLesson("Major");
            }
        });

        buttons.add(findViewById(R.id.c4));
        buttons.get(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLesson("Major");
            }
        });

        n_lessons = 4;

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
                return n_lessons;
            }

            @Override
            public void populate(View view, int index) {
                ((TextView)view).setText(buttons.get(index).getText());
            }

            @Override
            public void onNewItem(int index) {

            }
        });
    }
}