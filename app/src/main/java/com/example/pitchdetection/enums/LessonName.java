package com.example.pitchdetection.enums;

public enum LessonName {
    Major(1),
    Minor(2),
    Dominant(3),
    Progression145_C(4),
    Progression1645_C(5),
    Progression1514_C(6),
    Progression145_E(7);

    private final int value;

    LessonName(int v){
        this.value = v;
    }

    public String toString() {
        switch(this) {
            case Major:
                return "Major";
            case Minor:
                return "Minor";
            case Dominant:
                return "Dominant";
            case Progression145_C:
                return "Progresion145 C";
            case Progression1645_C:
                return "Progresion1645 C";
            case Progression1514_C:
                return "Progresion1541 C";
            case Progression145_E:
                return "Progresion145 E";
            default:
                return "";
        }
    }
}
