package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

public class Progression1514C extends Lesson{
    public Progression1514C() {
        init();
    }
    @Override
    void init() {
        Chord c_major = new Chord(NoteNameEnum.C, ChordTypeEnum.Major);
        c_major.add(new Note(2,1));
        c_major.add(new Note(4,2));
        c_major.add(new Note(5,3));

        Chord f_major = new Chord(NoteNameEnum.F, ChordTypeEnum.Major);
        f_major.add(new Note(0,1));
        f_major.add(new Note(3,2));
        f_major.add(new Note(5,3));
        f_major.add(new Note(4,3));

        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(2,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));

        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(f_major);

        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(f_major);

        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(f_major);

        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(f_major);
    }
}
