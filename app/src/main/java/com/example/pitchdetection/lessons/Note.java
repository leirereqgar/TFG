package com.example.pitchdetection.lessons;

import enums.NoteNameEnum;

public class Note {
    NoteNameEnum name;
    private int string;
    private int fret;

    Note(int s, int f) {
        string = s;
        fret = f;
    }

    public int getString() {
        return string;
    }

    public int getFret() {
        return fret;
    }
}
