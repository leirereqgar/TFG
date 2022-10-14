package com.example.pitchdetection;

public class ChordDetector {
    private static final int N_SEMITONES = 12;
    private static final int N_ACORDES   = 108;
    private int root_note, intervals;
    private ChordQuality quality;
    private float [] chromagram = new float[N_SEMITONES];
    private double [] chord = new double[N_ACORDES];
    private float [][] chord_profiles = new float[N_ACORDES][N_SEMITONES];
    private Double bias;


    public ChordDetector() {
        bias = 1.06;
        makeChordProfiles();
    }

    private void makeChordProfiles(){
        for(int i = 0; i < N_ACORDES; i++)
            for(int j = 0; j < N_SEMITONES; j++)
                chord_profiles[i][j] = 0;


        int root, third, fifth, seventh;
        int indice = 0;
        //Major chords
        for(int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+4) % 12;
            fifth = (i+7) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Minor chords
        for(int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+3) % 12;
            fifth = (i+6) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Diminished chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+3) % 12;
            fifth = (i+6) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Augmented chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+4) % 12;
            fifth = (i+8) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Sus2 chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+2) % 12;
            fifth = (i+7) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Sus4 chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root  = i % 12;
            third = (i+5) % 12;
            fifth = (i+7) % 12;

            chord_profiles[indice][root]  = 1;
            chord_profiles[indice][third] = 1;
            chord_profiles[indice][fifth] = 1;
        }

        //Major 7th chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root    = i % 12;
            third   = (i+4) % 12;
            fifth   = (i+7) % 12;
            seventh = (i+11) % 12;

            chord_profiles[indice][root]    = 1;
            chord_profiles[indice][third]   = 1;
            chord_profiles[indice][fifth]   = 1;
            chord_profiles[indice][seventh] = 1;
        }

        //Major 7th chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root    = i % 12;
            third   = (i+3) % 12;
            fifth   = (i+7) % 12;
            seventh = (i+10) % 12;

            chord_profiles[indice][root]    = 1;
            chord_profiles[indice][third]   = 1;
            chord_profiles[indice][fifth]   = 1;
            chord_profiles[indice][seventh] = 1;
        }

        //Dominant 7th chords
        for (int i = 0; i < N_SEMITONES; i++, indice++) {
            root    = i % 12;
            third   = (i+4) % 12;
            fifth   = (i+7) % 12;
            seventh = (i+10) % 12;

            chord_profiles[indice][root]    = 1;
            chord_profiles[indice][third]   = 1;
            chord_profiles[indice][fifth]   = 1;
            chord_profiles[indice][seventh] = 1;
        }
    }

    public void detectChord(float [] chroma) {
        for (int i = 0; i < N_SEMITONES; i++) {
            chromagram[i] = chroma[i];
        }

        classifyChromagram();
    }

    private void classifyChromagram() {
        int fifth, chord_index;
        //Remove som of the 5th note energy from chromagram
        for (int i = 0; i < N_SEMITONES; i++) {
            fifth = (i+7) % 12;
            chromagram[fifth] = chromagram[fifth] - (0.1f * chromagram[i]);

            if(chromagram[fifth] < 0f)
                chromagram[fifth] = 0f;
        }

        //major chords
        for (int i = 0; i < N_SEMITONES; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 3);

        // minor chords
        for (int i = N_SEMITONES; i < N_SEMITONES*2; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 3);

        // diminished 5th chords
        for (int i = N_SEMITONES*2; i < N_SEMITONES*3; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 3);

        // augmented 5th chords
        for (int i = N_SEMITONES*3; i < N_SEMITONES*4; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 3);

        // sus2 chords
        for (int i = 48; i < 60; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], 1.0, 3);

        // sus4 chords
        for (int i = 60; i < 72; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], 1.0, 3);

        // major 7th chords
        for (int i = 72; i < 84; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], 1.0, 4);

        // minor 7th chords
        for (int i = 84; i < 96; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 4);

        // dominant 7th chords
        for (int i = 96; i < 108; i++)
            chord[i] = calculateChordScore (chromagram, chord_profiles[i], bias, 4);

        chord_index = minimumIndex (chord, 108);

        // major
        if (chord_index < 12) {
            root_note = chord_index;
            quality = ChordQuality.MAJOR;
            intervals = 0;
        }

        // minor
        if ((chord_index >= 12) && (chord_index < 24)) {
            root_note = chord_index-12;
            quality = ChordQuality.MINOR;
            intervals = 0;
        }

        // diminished 5th
        if ((chord_index >= 24) && (chord_index < 36)) {
            root_note = chord_index-24;
            quality = ChordQuality.DIMINISHED5TH;
            intervals = 0;
        }

        // augmented 5th
        if ((chord_index >= 36) && (chord_index < 48)) {
            root_note = chord_index-36;
            quality = ChordQuality.AUGMENTED5TH;
            intervals = 0;
        }

        // sus2
        if ((chord_index >= 48) && (chord_index < 60)) {
            root_note = chord_index-48;
            quality = ChordQuality.SUSPENDED;
            intervals = 2;
        }

        // sus4
        if ((chord_index >= 60) && (chord_index < 72)) {
            root_note = chord_index-60;
            quality = ChordQuality.SUSPENDED;
            intervals = 4;
        }

        // major 7th
        if ((chord_index >= 72) && (chord_index < 84)) {
            root_note = chord_index-72;
            quality = ChordQuality.MAJOR;
            intervals = 7;
        }

        // minor 7th
        if ((chord_index >= 84) && (chord_index < 96)) {
            root_note = chord_index-84;
            quality = ChordQuality.MINOR;
            intervals = 7;
        }

        // dominant 7th
        if ((chord_index >= 96) && (chord_index < 108)) {
            root_note = chord_index-96;
            quality = ChordQuality.DOMINANT;
            intervals = 7;
        }
    }

    private double calculateChordScore(float[] chroma, float[] chord_profile, Double bias, int N) {
        double sum = 0;
        for (int i = 0; i < N_SEMITONES; i++) {
            sum = sum + ((1 - chord_profile[i]) * Math.pow(chroma[i],2));
        }

        return (Math.sqrt(sum) / ((N_SEMITONES-N) * bias));
    }

    private int minimumIndex(double [] array, int l) {
        double min = array[0];
        int min_index = 0;

        for (int i = 0; i < l; i++) {
            if(array[i] < min){
                min = array[i];
                min_index = i;
            }
        }

        return min_index;
    }
}
