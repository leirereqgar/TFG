package com.example.pitchdetection;

import java.util.ArrayList;

public class Chromagram {
    private ArrayList<Double> window;
    private ArrayList<Double> buffer;
    private ArrayList<Double> magnitude_spectrum;
    private ArrayList<Double> downsampled_input_audio_frame;
    private ArrayList<Double> chromagram;
    private double reference_frequency;
    private double[] note_frequencies = new double[12];
    private int buffer_size, sampling_frequency, input_audio_frame_size, down_sampled_audio_frame_size;
    private int num_harmonics, num_octaves, num_bins_to_search, num_samples_since_last_calculation;
    private int chroma_calculation_interval;
    private boolean chroma_ready;

    public Chromagram(int frame_size, int fs) {
        reference_frequency = 130.81278265;
        buffer_size = 8192;
        num_harmonics = 2;
        num_octaves = 2;
        num_bins_to_search = 2;

        // calculate note frequencies
        for (int i = 0; i < 12; i++)
        {
            note_frequencies[i] = reference_frequency * Math.pow(2,(((float) i) / 12));
        }

        // set up FFT
        //setupFFT();

        // set buffer size
        buffer = new ArrayList<>(buffer_size);

        // setup chromagram vector
        chromagram = new ArrayList<>(12);

        // initialise chromagram
        for (int i = 0; i < 12; i++) {
            chromagram.set(i, 0.0);
        }

        // setup magnitude spectrum vector
        magnitude_spectrum = new ArrayList<>((buffer_size / 2) + 1);

        // make window function
        makeHammingWindow();

        // set sampling frequency
        setSamplingFrequency (fs);

        // set input audio frame size
        setInputAudioFrameSize (frame_size);

        // initialise num samples counter
        num_samples_since_last_calculation = 0;

        // set chroma calculation interval (in samples at the input audio sampling frequency)
        chroma_calculation_interval = 4096;

        // initialise chroma ready variable
        chroma_ready = false;
    }

    public void processAudioFrame(ArrayList<Double> input_audio_frame) {
        // our default state is that the chroma is not ready
        chroma_ready = false;

        // downsample the input audio frame by 4
        downSampleFrame(input_audio_frame);

        // move samples back
        for (int i = 0; i < buffer_size - down_sampled_audio_frame_size; i++)
            buffer.set(i, buffer.get(i + down_sampled_audio_frame_size));

        int n = 0;

        // add new samples to buffer
        for (int i = (buffer_size - down_sampled_audio_frame_size); i < buffer_size; i++) {
            buffer.set(i, downsampled_input_audio_frame.get(n));
            n++;
        }

        // add number of samples from calculation
        num_samples_since_last_calculation += input_audio_frame_size;

        // if we have had enough samples
        if (num_samples_since_last_calculation >= chroma_calculation_interval){
            // calculate the chromagram
            calculateChromagram();

            // reset num samples counter
            num_samples_since_last_calculation = 0;
        }
    }

    public void setInputAudioFrameSize(int frame_size){
        input_audio_frame_size = frame_size;
        downsampled_input_audio_frame = new ArrayList<>(input_audio_frame_size / 4);
        down_sampled_audio_frame_size = downsampled_input_audio_frame.size();
    }

    public void setSamplingFrequency(int fs){
        sampling_frequency = fs;
    }

    public void setChromaCalculationInterval(int num_samples){
        chroma_calculation_interval = num_samples;
    }

    public ArrayList<Double> getChromagram(){
        return chromagram;
    }

    public boolean isReady(){
        return chroma_ready;
    }

    //private void setupFFT(){}

    private void calculateChromagram(){
        calculateMagnitudeSpectrum();

        double divisor_ratio = (sampling_frequency / 4.0) / (buffer_size * 1.0);

        for (int i = 0; i < 12; i++) {
            double chroma_sum = 0;
            for (int octave = 0; octave <= num_octaves; octave++) {
                double note_sum = 0.0;
                for (int harmonic = 1; harmonic <= num_harmonics; harmonic++) {
                    int center_bin = (int) round((note_frequencies[i] * octave * harmonic) / divisor_ratio);
                    int min_bin = center_bin - (num_bins_to_search * harmonic);
                    int max_bin = center_bin + (num_bins_to_search * harmonic);
                    double max_val = 0;
                    for (int k = min_bin; k < max_bin; k++) {
                        if(magnitude_spectrum.get(k) > max_val)
                            max_val = magnitude_spectrum.get(k);
                    }
                    note_sum += (max_val / (double) harmonic);
                }
                chroma_sum += note_sum;
            }
            chromagram.set(i, chroma_sum);
        }
        chroma_ready = true;
    }

    private void calculateMagnitudeSpectrum() {

    }

    private void downSampleFrame(ArrayList<Double> input_audio_frame) {
        ArrayList<Double> filtered_frame = new ArrayList<>(input_audio_frame);

        float b0,b1,b2,a1,a2;
        double x_1, x_2, y_1, y_2;

        b0 = 0.2929f;
        b1 = 0.5858f;
        b2 = 0.2929f;
        a1 = -0.0000f;
        a2 = 0.1716f;

        x_1 = 0;
        x_2 = 0;
        y_1 = 0;
        y_2 = 0;

        for (int i = 0; i < input_audio_frame_size; i++) {
            filtered_frame.set(i, input_audio_frame.get(i) * b0 + x_1 * b1 + x_2 * b2 - y_1 * a1 - y_2 * a2);
            x_2 = x_1;
            x_1 = input_audio_frame.get(i);
            y_2 = y_1;
            y_1 = filtered_frame.get(i);
        }

        for (int i = 0; i < input_audio_frame_size; i++) {
            downsampled_input_audio_frame.set(i, filtered_frame.get(i) * 4);
        }
    }

    private void makeHammingWindow(){
        window = new ArrayList<Double>(buffer_size);
        for (int n = 0; n < buffer_size; n++) {
            window.set(n, 0.54 - 0.46 * Math.cos(2 * Math.PI * (((double) n) / ((double) buffer_size))));
        }
    }

    private double round(double val) {
        return Math.floor(val + 0.5);
    }
}
/*
        void Chromagram::setupFFT()
        {
        // ------------------------------------------------------
        #ifdef USE_FFTW
        complexIn = (fftw_complex*) fftw_malloc (sizeof (fftw_complex) * bufferSize);		// complex array to hold fft data
        complexOut = (fftw_complex*) fftw_malloc (sizeof (fftw_complex) * bufferSize);	// complex array to hold fft data
        p = fftw_plan_dft_1d (bufferSize, complexIn, complexOut, FFTW_FORWARD, FFTW_ESTIMATE);	// FFT plan initialisation
        #endif

        // ------------------------------------------------------
        #ifdef USE_KISS_FFT
        // initialise the fft time and frequency domain audio frame arrays
        fftIn = new kiss_fft_cpx[bufferSize];
        fftOut = new kiss_fft_cpx[bufferSize];
        cfg = kiss_fft_alloc (bufferSize,0,0,0);
        #endif
        }*/

/*
//==================================================================================
        void Chromagram::calculateMagnitudeSpectrum()
        {

        #ifdef USE_FFTW
        // -----------------------------------------------
        // FFTW VERSION
        // -----------------------------------------------
        int i = 0;

        for (int i = 0; i < bufferSize; i++)
        {
        complexIn[i][0] = buffer[i] * window[i];
        complexIn[i][1] = 0.0;
        }

        // execute fft plan, i.e. compute fft of buffer
        fftw_execute (p);

        // compute first (N/2)+1 mag values
        for (i = 0; i < (bufferSize / 2) + 1; i++)
        {
        magnitudeSpectrum[i] = sqrt (pow (complexOut[i][0], 2) + pow (complexOut[i][1], 2));
        magnitudeSpectrum[i] = sqrt (magnitudeSpectrum[i]);
        }
        #endif


        #ifdef USE_KISS_FFT
        // -----------------------------------------------
        // KISS FFT VERSION
        // -----------------------------------------------
        int i = 0;

        for (int i = 0;i < bufferSize; i++)
        {
        fftIn[i].r = buffer[i] * window[i];
        fftIn[i].i = 0.0;
        }

        // execute kiss fft
        kiss_fft (cfg, fftIn, fftOut);

        // compute first (N/2)+1 mag values
        for (i = 0; i < (bufferSize / 2) + 1; i++)
        {
        magnitudeSpectrum[i] = sqrt (pow (fftOut[i].r, 2) + pow (fftOut[i].i, 2));
        magnitudeSpectrum[i] = sqrt (magnitudeSpectrum[i]);
        }
        #endif
        }
*/