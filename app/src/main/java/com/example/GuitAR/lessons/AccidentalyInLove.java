package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class AccidentalyInLove extends Lesson{
    public AccidentalyInLove() {
        init();
    }
    @Override
    void init() {
        code = ChordTypeEnum.Major;

        chords = new ArrayList<>();

        Chord a_major = new Chord(NoteNameEnum.A, ChordTypeEnum.Major);
        a_major.add(new Note(4,2));
        a_major.add(new Note(3,2));
        a_major.add(new Note(2,2));
        Chord a_minor = new Chord(NoteNameEnum.A, ChordTypeEnum.Minor);
        a_minor.add(new Note(2,1));
        a_minor.add(new Note(4,2));
        a_minor.add(new Note(3,2));
        Chord c_major = new Chord(NoteNameEnum.C, ChordTypeEnum.Major);
        c_major.add(new Note(2,1));
        c_major.add(new Note(4,2));
        c_major.add(new Note(5,3));
        Chord d_major = new Chord(NoteNameEnum.D, ChordTypeEnum.Major);
        d_major.add(new Note(3,2));
        d_major.add(new Note(1,2));
        d_major.add(new Note(2,3));
        Chord e_minor = new Chord(NoteNameEnum.E, ChordTypeEnum.Minor);
        e_minor.add(new Note(5,2));
        e_minor.add(new Note(4,2));
        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(5,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));

        chords.add(g_major);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(e_minor);
        chords.add(a_major);
        chords.add(c_major);

        chords.add(g_major);
        chords.add(c_major);
        chords.add(g_major);
        chords.add(c_major);
        chords.add(e_minor);
        chords.add(a_major);
        chords.add(d_major);
        chords.add(c_major);

        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(d_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(d_major);
        chords.add(g_major);
        chords.add(a_minor);
        chords.add(c_major);
        chords.add(d_major);



    }
}
