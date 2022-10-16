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
    ChordDetector cd;

public:
    ChordProcessor();
    int * chordDetection(double *samples);

    double * window(double * samples, int l);
};


#endif //PITCHDETECTION_CHORDPROCESSOR_H
