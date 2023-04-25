package com.example.pitchdetection.lessons;

import java.util.ArrayList;

public class Chord {
    ArrayList<Note> note_array;

    Chord(){
        note_array = new ArrayList<>();
    }

    public Note get(int i) {
        return note_array.get(i);
    }

    public void add(Note n) {
        note_array.add(n);
    }

    public int size() {
        return note_array.size();
    }
}
