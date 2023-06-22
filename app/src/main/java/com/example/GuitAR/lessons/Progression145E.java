package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class Progression145E extends Lesson{
    public Progression145E() {
        init();
    }
    @Override
    void init() {
        code = ChordTypeEnum.Major;

        chords = new ArrayList<>();

        Chord e_major = new Chord(NoteNameEnum.E, ChordTypeEnum.Major);
        e_major.add(new Note(3,1));
        e_major.add(new Note(5,2));
        e_major.add(new Note(4,2));
        chords.add(e_major);

        Chord a_major = new Chord(NoteNameEnum.A, ChordTypeEnum.Major);
        a_major.add(new Note(4,2));
        a_major.add(new Note(3,2));
        a_major.add(new Note(2,2));
        chords.add(a_major);

        Chord b_major = new Chord(NoteNameEnum.B, ChordTypeEnum.Major);
        b_major.add(new Note(0,2));
        b_major.add(new Note(4,4));
        b_major.add(new Note(3,4));
        b_major.add(new Note(2,4));
        chords.add(b_major);

        chords.add(e_major);
        chords.add(a_major);
        chords.add(b_major);
        chords.add(e_major);
        chords.add(a_major);
        chords.add(b_major);
        chords.add(e_major);
        chords.add(a_major);
        chords.add(b_major);
    }
}
