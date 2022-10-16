#include "ChordProcessor.h"

ChordProcessor::ChordProcessor() {
    chromagram = Chromagram(FRAMESIZE, SAMPLERATE);
}

int * ChordProcessor::chordDetection(double *samples) {
    int * salida = new int[2];

    chromagram.processAudioFrame(samples);
    if(chromagram.isReady()) {
        cd.detectChord(chromagram.getChromagram());
        salida[0] = cd.rootNote;
        salida[1] = cd.quality;
    }

    return salida;
}

double * ChordProcessor::window(double *samples, int l) {
    for(int i = 0; i < l; i++) {
        samples[i] = (0.5 * (1.0 - cos(2.0*M_PI*(double)i/(double)(l - 1)))) * samples[i];
    }
    return samples;
}



