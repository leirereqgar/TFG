package com.example.pitchdetection;

import java.util.ArrayList;


//Mirar convertir en singleton
public class GuitarNotes {
    private static ArrayList<NoteOld> noteOlds;

    private static final GuitarNotes instance = new GuitarNotes();

    public static GuitarNotes getInstance() {
        return instance;
    }
    
    private GuitarNotes() {
        noteOlds = new ArrayList<NoteOld>();
        createNotes();
    }

    private void createNotes() {
        noteOlds.add(new NoteOld(-1000f, "Control"));

        noteOlds.add(new NoteOld(82.41f,   "E2"));
        noteOlds.add(new NoteOld(87.31f,   "F2"));
        noteOlds.add(new NoteOld(92.5f,"F#2"));
        noteOlds.add(new NoteOld(98f,      "G2"));
        noteOlds.add(new NoteOld(103.83f, "G#2"));
        noteOlds.add(new NoteOld(110f,     "A2"));
        noteOlds.add(new NoteOld(116.54f, "A#2"));
        noteOlds.add(new NoteOld(123.47f,  "B2"));
        noteOlds.add(new NoteOld(130.81f, "C3"));
        noteOlds.add(new NoteOld(138.59f, "C#3"));
        noteOlds.add(new NoteOld(146.83f, "D3"));
        noteOlds.add(new NoteOld(155.56f, "D#3"));
        noteOlds.add(new NoteOld(164.81f, "E3"));
        noteOlds.add(new NoteOld(174.61f, "F3"));
        noteOlds.add(new NoteOld(185f, "F#3"));
        noteOlds.add(new NoteOld(196f, "G3"));
        noteOlds.add(new NoteOld(207.65f, "G#3"));
        noteOlds.add(new NoteOld(220f, "A3"));
        noteOlds.add(new NoteOld(233.08f, "A#3"));
        noteOlds.add(new NoteOld(246.94f, "B3"));
        noteOlds.add(new NoteOld(261.63f, "C4"));
        noteOlds.add(new NoteOld(277.18f, "C#4"));
        noteOlds.add(new NoteOld(293.66f, "D4"));
        noteOlds.add(new NoteOld(311.13f, "D#4"));
        noteOlds.add(new NoteOld(329.63f, "E4"));
        noteOlds.add(new NoteOld(349.23f, "F4"));
        noteOlds.add(new NoteOld(369.99f, "F#4"));
        noteOlds.add(new NoteOld(392f, "G4"));
        noteOlds.add(new NoteOld(415.3f, "G#4"));
        noteOlds.add(new NoteOld(440f, "A4"));
        noteOlds.add(new NoteOld(466.16f, "A#4"));
        noteOlds.add(new NoteOld(493.88f, "B4"));

        noteOlds.add(new NoteOld(1000f, "Control"));
    }

    public String getNoteName(float p) {
        boolean in_range = false;
        NoteOld n = noteOlds.get(noteOlds.size()-1);

        in_range = noteOlds.get(0).checkPitch(noteOlds.get(1), noteOlds.get(noteOlds.size()-1), p);

        for (int i = 1; i < (noteOlds.size() - 1) && !in_range; i++) {
            in_range = noteOlds.get(i).checkPitch(noteOlds.get(i + 1), noteOlds.get(i-1), p);

            if(in_range) {
                n = noteOlds.get(i);
            }
        }

        return n.getName();
    }
}
