package com.example.pitchdetection.lessons;

import enums.ChordTypeEnum;

public class Minor extends Lesson{
    @Override
    public void init(){
        code = ChordTypeEnum.Minor;
        
        Chord c_minor = new Chord();
        c_minor.add(new Note(0,3));
        c_minor.add(new Note(2,4));
        c_minor.add(new Note(4,5));
        c_minor.add(new Note(3,5));
        chords.add(c_minor);

        Chord d_minor = new Chord();
        d_minor.add(new Note(1,1));
        d_minor.add(new Note(3,2));
        d_minor.add(new Note(2,3));
        chords.add(d_minor);   
        
        Chord e_minor = new Chord();
        d_minor.add(new Note(5,2));
        d_minor.add(new Note(4,2));
        chords.add(e_minor);
        
        Chord f_minor = new Chord();
        f_minor.add(new Note(0,1));
        f_minor.add(new Note(5,3));
        f_minor.add(new Note(4,3));
        chords.add(f_minor);
        
        Chord g_minor = new Chord();
        g_minor.add(new Note(5,1));
        g_minor.add(new Note(6,2));
        g_minor.add(new Note(2,2));
        g_minor.add(new Note(1,2));
        chords.add(g_minor);
        
        Chord a_minor = new Chord();
        a_minor.add(new Note(2,1));
        a_minor.add(new Note(4,2));
        a_minor.add(new Note(3,2));
        chords.add(a_minor);
        
        Chord b_minor = new Chord();
        a_minor.add(new Note(0,2));
        a_minor.add(new Note(2,3));
        a_minor.add(new Note(4,4));
        a_minor.add(new Note(3,4));
        chords.add(b_minor);

    }
}
