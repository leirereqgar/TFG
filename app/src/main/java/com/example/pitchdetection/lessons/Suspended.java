package com.example.pitchdetection.lessons;

import com.example.pitchdetection.enums.ChordTypeEnum;
import com.example.pitchdetection.enums.NoteNameEnum;

import java.util.ArrayList;

public class Suspended extends Lesson{
    @Override
    void init() {
        code = ChordTypeEnum.Suspended;

        chords = new ArrayList<>();

        Chord c_suspended = new Chord(NoteNameEnum.C, ChordTypeEnum.Suspended);
        c_suspended.add(new Note(2,1));
        c_suspended.add(new Note(5,3));
        c_suspended.add(new Note(4,3));
        c_suspended.add(new Note(1,3));
        chords.add(c_suspended);

        Chord d_suspended = new Chord(NoteNameEnum.D, ChordTypeEnum.Suspended);
        d_suspended.add(new Note(3,2));
        d_suspended.add(new Note(2,3));
        d_suspended.add(new Note(1,3));
        chords.add(d_suspended);

        Chord e_suspended = new Chord(NoteNameEnum.E, ChordTypeEnum.Suspended);
        e_suspended.add(new Note(5,2));
        e_suspended.add(new Note(4,2));
        e_suspended.add(new Note(3,2));
        chords.add(e_suspended);

        Chord f_suspended = new Chord(NoteNameEnum.F, ChordTypeEnum.Suspended);
        f_suspended.add(new Note(0,1));
        f_suspended.add(new Note(5,3));
        f_suspended.add(new Note(4,3));
        f_suspended.add(new Note(3,3));
        chords.add(f_suspended);

        Chord g_suspended = new Chord(NoteNameEnum.G, ChordTypeEnum.Suspended);
        g_suspended.add(new Note(6,3));
        g_suspended.add(new Note(2,3));
        g_suspended.add(new Note(1,3));
        chords.add(g_suspended);

        Chord a_suspended = new Chord(NoteNameEnum.A, ChordTypeEnum.Suspended);
        a_suspended.add(new Note(4,2));
        a_suspended.add(new Note(3,2));
        chords.add(a_suspended);

        Chord b_suspended = new Chord(NoteNameEnum.B, ChordTypeEnum.Suspended);
        b_suspended.add(new Note(0,2));
        b_suspended.add(new Note(4,4));
        b_suspended.add(new Note(3,4));
        chords.add(b_suspended);
    }
}
