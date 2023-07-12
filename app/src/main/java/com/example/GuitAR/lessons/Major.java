package com.example.GuitAR.lessons;

import com.example.GuitAR.enums.ChordTypeEnum;
import com.example.GuitAR.enums.NoteNameEnum;

import java.util.ArrayList;

public class Major extends Lesson{

    public Major(){
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

        Chord d_major = new Chord(NoteNameEnum.D, ChordTypeEnum.Major);
        d_major.add(new Note(3,2));
        d_major.add(new Note(1,2));
        d_major.add(new Note(2,3));
        chords.add(d_major);

        Chord e_major = new Chord(NoteNameEnum.E, ChordTypeEnum.Major);
        e_major.add(new Note(3,1));
        e_major.add(new Note(5,2));
        e_major.add(new Note(4,2));
        chords.add(e_major);

//        Chord f_major = new Chord(NoteNameEnum.F, ChordTypeEnum.Major);
//        f_major.add(new Note(0,1));
//        f_major.add(new Note(3,2));
//        f_major.add(new Note(5,3));
//        f_major.add(new Note(4,3));
//        chords.add(f_major);

        Chord g_major = new Chord(NoteNameEnum.G, ChordTypeEnum.Major);
        g_major.add(new Note(5,2));
        g_major.add(new Note(6,3));
        g_major.add(new Note(1,3));
        chords.add(g_major);

        Chord a_major = new Chord(NoteNameEnum.A, ChordTypeEnum.Major);
        a_major.add(new Note(4,2));
        a_major.add(new Note(3,2));
        a_major.add(new Note(2,2));
        chords.add(a_major);

//        Chord b_major = new Chord(NoteNameEnum.B, ChordTypeEnum.Major);
//        b_major.add(new Note(0,2));
//        b_major.add(new Note(4,4));
//        b_major.add(new Note(3,4));
//        b_major.add(new Note(2,4));
//        chords.add(b_major);
    }
}
