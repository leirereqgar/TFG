package com.example.pitchdetection;

public class Note {
    private float pitch;
    private String name;

    public Note(float p, String n) {
        this.pitch = p;
        this.name = n;
    }

    public boolean checkPitch(Note posterior, Note anterior, float p) {
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
