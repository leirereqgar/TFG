package com.example.pitchdetection;

import java.util.ArrayList;


//Mirar convertir en singleton
public class GuitarNotes {
    private static ArrayList<Note> notes;

    private static final GuitarNotes instance = new GuitarNotes();

    public static GuitarNotes getInstance() {
        return instance;
    }
    
    private GuitarNotes() {
        notes = new ArrayList<Note>();
        createNotes();
    }

    private void createNotes() {
        notes.add(new Note(-1000f, "Control"));

        notes.add(new Note(82.41f,   "E2"));
        notes.add(new Note(87.31f,   "F2"));
        notes.add(new Note(92.5f,"F#2"));
        notes.add(new Note(98f,      "G2"));
        notes.add(new Note(103.83f, "G#2"));
        notes.add(new Note(110f,     "A2"));
        notes.add(new Note(116.54f, "A#2"));
        notes.add(new Note(123.47f,  "B2"));
        notes.add(new Note(130.81f, "C3"));
        notes.add(new Note(138.59f, "C#3"));
        notes.add(new Note(146.83f, "D3"));
        notes.add(new Note(155.56f, "D#3"));
        notes.add(new Note(164.81f, "E3"));
        notes.add(new Note(174.61f, "F3"));
        notes.add(new Note(185f, "F#3"));
        notes.add(new Note(196f, "G3"));
        notes.add(new Note(207.65f, "G#3"));
        notes.add(new Note(220f, "A3"));
        notes.add(new Note(233.08f, "A#3"));
        notes.add(new Note(246.94f, "B3"));
        notes.add(new Note(261.63f, "C4"));
        notes.add(new Note(277.18f, "C#4"));
        notes.add(new Note(293.66f, "D4"));
        notes.add(new Note(311.13f, "D#4"));
        notes.add(new Note(329.63f, "E4"));
        notes.add(new Note(349.23f, "F4"));
        notes.add(new Note(369.99f, "F#4"));
        notes.add(new Note(392f, "G4"));
        notes.add(new Note(415.3f, "G#4"));
        notes.add(new Note(440f, "A4"));
        notes.add(new Note(466.16f, "A#4"));
        notes.add(new Note(493.88f, "B4"));

        notes.add(new Note(1000f, "Control"));
    }

    public String getNoteName(float p) {
        boolean in_range = false;
        Note n = notes.get(notes.size()-1);

        in_range = notes.get(0).checkPitch(notes.get(1), notes.get(notes.size()-1), p);

        for (int i = 1; i < (notes.size() - 1) && !in_range; i++) {
            in_range = notes.get(i).checkPitch(notes.get(i + 1), notes.get(i-1), p);

            if(in_range) {
                n = notes.get(i);
            }
        }

        return n.getName();
    }
}
