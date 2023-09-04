package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class Zombie extends Lesson{
    public Zombie() {
        init();
    }
    @Override
    void init() {
        chords = new ArrayList<>();

        Chord c_major = new Chord(NoteNameEnum.C, ChordTypeEnum.Major);
        c_major.add(new Note(2,1));
        c_major.add(new Note(4,2));
        c_major.add(new Note(5,3));

        Chord d_major = new Chord(NoteNameEnum.D, ChordTypeEnum.Major);
        d_major.add(new Note(3,2));
        d_major.add(new Note(1,2));
        d_major.add(new Note(2,3));

        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(5,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));

        Chord e_minor = new Chord(NoteNameEnum.E, ChordTypeEnum.Minor);
        e_minor.add(new Note(5,2));
        e_minor.add(new Note(4,2));


        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);

        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);

        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);
        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);
        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);
        chords.add(e_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(d_major);

    }
}
