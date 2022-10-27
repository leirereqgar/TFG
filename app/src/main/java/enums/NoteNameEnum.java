package enums;

public enum NoteNameEnum {
    A(0), A_SHARP(1),   B(2),  C(3), C_SHARP(4), D(5), D_SHARP(6), E(7), F(8), F_SHARP(9), G(10), G_SHARP(11), NO_NOTE(-1);

    private int value;

    NoteNameEnum(int i) {
        value = i;
    }

    public int getValue(){ return value; }

    public static NoteNameEnum fromInteger(int integerValue) {
        switch(integerValue) {
            case 0:
                return A;
            case 1:
                return A_SHARP;
            case 2:
                return B;
            case 3:
                return C;
            case 4:
                return C_SHARP;
            case 5:
                return D;
            case 6:
                return D_SHARP;
            case 7:
                return E;
            case 8:
                return F;
            case 9:
                return F_SHARP;
            case 10:
                return G;
            case 11:
                return G_SHARP;
            case -1:
            default:
                return NO_NOTE;
        }
    }
}
