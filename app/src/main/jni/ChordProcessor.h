#ifndef PITCHDETECTION_CHORDPROCESSOR_H
#define PITCHDETECTION_CHORDPROCESSOR_H

#include "ChordDetection/Chromagram.h"
#include "ChordDetection/ChordDetector.h"


class ChordProcessor {
private:
    const int FRAMESIZE  = 512;
    const int SAMPLERATE = 44100;
    const int NUMHARMONICS = 2;
    const int NUMOCTAVES = 2;
    const int NUMBINSTOSEARCH = 2;
    ChordProcessor * chordProcessor;
    Chromagram chromagram = Chromagram(0, 0);
    double * chroma_vector = new double [12];
    int * chord_output = new int[2];
    ChordDetector cd;

public:
    ChordProcessor();
    int* chordDetection(double* samples, double* spectrumSamples);
    double* getChromagram();

    static double* bandPassFilter(double* samples, float lowCutOffFreq, float highCutOffFreq, int sampleRate, int frameSize);
    static double* removeZeroFrequency(double* samples);

    static double* window(double* samples, int l);
    static double* fft(double* inputReal, int length, bool DIRECT);
    static double* fft(double* inputReal, double* inputImag, int length, bool DIRECT);
    static int bitReverseReference(int j, int nu);

    /**Audio Features**/
    static double getAverageLevel(double samples[], int length);
    static double getSpectralFlatness(double* inputSamples, int vectorLength);

};


#endif //PITCHDETECTION_CHORDPROCESSOR_H
