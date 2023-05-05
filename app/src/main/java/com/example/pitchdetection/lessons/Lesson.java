package com.example.pitchdetection.lessons;

import java.util.ArrayList;

import com.example.pitchdetection.enums.ChordTypeEnum;

public abstract class Lesson {
    ChordTypeEnum code;
    ArrayList<Chord> chords;

    public abstract void init();

    public ChordTypeEnum getCode() {
        return code;
    }

    public Chord getChord(int i) {
        return chords.get(i);
    }
}
