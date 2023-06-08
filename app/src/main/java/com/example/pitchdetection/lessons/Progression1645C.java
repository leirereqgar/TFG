package com.example.pitchdetection.lessons;

import com.example.pitchdetection.enums.ChordTypeEnum;
import com.example.pitchdetection.enums.NoteNameEnum;

import java.util.ArrayList;

public class Progression1645C extends Lesson{
    public Progression1645C() {
        init();
    }

    @Override
    void init() {
        code = ChordTypeEnum.Major;

        chords = new ArrayList<>();

        Chord c_major = new Chord(NoteNameEnum.C, ChordTypeEnum.Major);
        c_major.add(new Note(2,1));
        c_major.add(new Note(4,2));
        c_major.add(new Note(5,3));
        chords.add(c_major);

        Chord a_minor = new Chord(NoteNameEnum.A, ChordTypeEnum.Minor);
        a_minor.add(new Note(2,1));
        a_minor.add(new Note(4,2));
        a_minor.add(new Note(3,2));
        chords.add(a_minor);

        Chord f_major = new Chord(NoteNameEnum.F, ChordTypeEnum.Major);
        f_major.add(new Note(0,1));
        f_major.add(new Note(3,2));
        f_major.add(new Note(5,3));
        f_major.add(new Note(4,3));
        chords.add(f_major);

        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(2,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));
        chords.add(g_major);

        chords.add(c_major);
        chords.add(a_minor);
        chords.add(f_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(a_minor);
        chords.add(f_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(a_minor);
        chords.add(f_major);
        chords.add(g_major);
    }
}
