package com.example.pitchdetection.enums;

public enum NoteNameEnum {
    NO_NOTE(-1),
    C(0),C_SHARP(1),
    D(2),D_SHARP(3),
    E(4),
    F(5),F_SHARP(6),
    G(7),G_SHARP(8),
    A(9),A_SHARP(11),
    B(11);

    private int value;

    NoteNameEnum(int i) {
        value = i;
    }

    public int getValue(){ return value; }

    public static NoteNameEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return C;
            case 1:
                return C_SHARP;
            case 2:
                return D;
            case 3:
                return D_SHARP;
            case 4:
                return E;
            case 5:
                return F;
            case 6:
                return F_SHARP;
            case 7:
                return G;
            case 8:
                return G_SHARP;
            case 9:
                return A;
            case 10:
                return A_SHARP;
            case 11:
                return B;
            default:
                return NO_NOTE;
        }
    }
}
