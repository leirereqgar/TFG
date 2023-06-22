package com.example.GuitAR.lessons;

import java.util.ArrayList;

import com.example.GuitAR.enums.ChordTypeEnum;

public abstract class Lesson {
    ChordTypeEnum code;
    ArrayList<Chord> chords;

    abstract void init();

    public ChordTypeEnum getCode() {
        return code;
    }

    public Chord getChord(int i) {
        return chords.get(i);
    }

    public int size() {
        return chords.size();
    }
}
