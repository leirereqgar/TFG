package com.example.pitchdetection.enums;

public enum ChordTypeEnum {
    Minor(0),
    Major(1),
    Suspended(2),
    Dominant(3),
    Diminished5th(4),
    Augmented5th(5),
    NoChord(-1);

    private final int value;
    public static int numberOfChordTypes = 10;

    ChordTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ChordTypeEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return Minor;
            case 1:
                return Major;
            case 2:
                return Suspended;
            case 3:
                return Dominant;
            case 5:
                return Diminished5th;
            case 6:
                return Augmented5th;
            case -1:
            default:
                return NoChord;
        }
    }

    public String toString() {
        switch(this) {
            case Minor:
                return "Minor";
            case Major:
                return "Major";
            case Suspended:
                return "Suspended";
            case Dominant:
                return "Dominant";
            case Diminished5th:
                return "Dim";
            case Augmented5th:
                return "Aug";
            case NoChord:
            default:
                return "---";
        }
    }
}
