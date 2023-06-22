package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class Chord {
    NoteNameEnum name;
    ChordTypeEnum type;
    ArrayList<Note> note_array;

    Chord(NoteNameEnum n, ChordTypeEnum t){
        name = n;
        type = t;
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

    public NoteNameEnum getName() {
        return name;
    }

    public ChordTypeEnum getType() {
        return type;
    }

    public int numFrets() {
        return note_array.get(note_array.size()-1).getFret();
    }

    public String toString() {
        return name.toString() + " " + type.toString();
    }
}
