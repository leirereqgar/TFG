package com.example.GuitAR.enums;

public enum LessonName {
    Major(1),
    Minor(2),
    Dominant(3),
    ShakeItOff(4),
    IGottaFeelin(5),
    Zombie(6),
    AccidentalyInLove(7);

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
            case ShakeItOff:
                return "Shake It off";
            case IGottaFeelin:
                return "I Gotta Feelin";
            case Zombie:
                return "Zombie";
            case AccidentalyInLove:
                return "Accidentaly In Love";
            default:
                return "";
        }
    }
}
