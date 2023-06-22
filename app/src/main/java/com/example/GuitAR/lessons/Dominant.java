package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class Dominant extends Lesson{
    @Override
    public void init() {
        code = ChordTypeEnum.Dominant;

        chords = new ArrayList<>();

        Chord c_dominant = new Chord(NoteNameEnum.C, ChordTypeEnum.Dominant);
        c_dominant.add(new Note(2,1));
        c_dominant.add(new Note(4,2));
        c_dominant.add(new Note(5,3));
        c_dominant.add(new Note(3,3));
        chords.add(c_dominant);

        Chord d_dominant = new Chord(NoteNameEnum.D, ChordTypeEnum.Dominant);
        d_dominant.add(new Note(2,1));
        d_dominant.add(new Note(3,2));
        d_dominant.add(new Note(1,2));
        chords.add(d_dominant);

        Chord e_dominant = new Chord(NoteNameEnum.E, ChordTypeEnum.Dominant);
        e_dominant.add(new Note(3,1));
        e_dominant.add(new Note(5,2));
        chords.add(e_dominant);

        Chord f_dominant = new Chord(NoteNameEnum.F, ChordTypeEnum.Dominant);
        f_dominant.add(new Note(0,1));
        f_dominant.add(new Note(3,2));
        f_dominant.add(new Note(5,3));
        chords.add(f_dominant);

        Chord g_dominant = new Chord(NoteNameEnum.G, ChordTypeEnum.Dominant);
        g_dominant.add(new Note(1,1));
        g_dominant.add(new Note(5,2));
        g_dominant.add(new Note(6,3));
        chords.add(g_dominant);

        Chord a_dominant = new Chord(NoteNameEnum.A, ChordTypeEnum.Dominant);
        a_dominant.add(new Note(4,2));
        a_dominant.add(new Note(2,2));
        chords.add(a_dominant);

        Chord b_dominant = new Chord(NoteNameEnum.B, ChordTypeEnum.Dominant);
        b_dominant.add(new Note(4,1));
        b_dominant.add(new Note(5,2));
        b_dominant.add(new Note(3,2));
        b_dominant.add(new Note(1,2));
        chords.add(b_dominant);
    }
}
