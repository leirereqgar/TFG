package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class ShakeItOff extends Lesson{
    public ShakeItOff() {
        init();
    }
    @Override
    void init() {
        code = ChordTypeEnum.Major;

        chords = new ArrayList<>();

        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(2,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));

        Chord c_major = new Chord(NoteNameEnum.C, ChordTypeEnum.Major);
        c_major.add(new Note(2,1));
        c_major.add(new Note(4,2));
        c_major.add(new Note(5,3));

        Chord a_minor = new Chord(NoteNameEnum.A, ChordTypeEnum.Minor);
        a_minor.add(new Note(2,1));
        a_minor.add(new Note(4,2));
        a_minor.add(new Note(3,2));

        Chord d_major = new Chord(NoteNameEnum.D, ChordTypeEnum.Major);
        d_major.add(new Note(3,2));
        d_major.add(new Note(1,2));
        d_major.add(new Note(2,3));

        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);

        chords.add(g_major);
        chords.add(d_major);
        chords.add(a_minor);
        chords.add(c_major);

        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(g_major);
    }
}
