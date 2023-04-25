package com.example.pitchdetection;

public class NoteOld {
    private float pitch;
    private String name;

    public NoteOld(float p, String n) {
        this.pitch = p;
        this.name = n;
    }

    public boolean checkPitch(NoteOld posterior, NoteOld anterior, float p) {
        boolean in_range = false;

        if (anterior.getPitch() < p && pitch <= p && p < posterior.getPitch())
            in_range = true;

        return in_range;
    }

    public float getPitch() {
        return this.pitch;
    }

    public String getName() {
        return this.name;
    }
}
