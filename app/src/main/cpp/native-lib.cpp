#include <jni.h>
#include "ChordProcessor.h"

extern "C"{
    ChordProcessor * cd = new ChordProcessor();

    jintArray Java_com_example_GuitAR_services_ChordRecognitionService_chordDetection(JNIEnv *env, jclass clazz, jdoubleArray samples, jdoubleArray spectrum_samples) {
        jintArray output = env->NewIntArray(2);

        env->SetIntArrayRegion(output,0,2, cd->chordDetection(env->GetDoubleArrayElements(samples,0), env->GetDoubleArrayElements(spectrum_samples,0)));

        return output;
    }

    jdoubleArray Java_com_example_GuitAR_services_ChordRecognitionService_getChromagram(JNIEnv *env, jclass clazz) {
        jdoubleArray output = env->NewDoubleArray(12);
        env->SetDoubleArrayRegion(output,0,12,cd->getChromagram());
        return output;
    }

    jdoubleArray Java_com_example_GuitAR_services_ChordRecognitionService_window(JNIEnv *env, jclass clazz, jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        jdoubleArray output = env->NewDoubleArray(length);

        env->SetDoubleArrayRegion(output, 0, length, cd->window(env->GetDoubleArrayElements(samples, 0), length));

        return output;
    }

    jdoubleArray Java_com_example_GuitAR_services_ChordRecognitionService_fft(JNIEnv *env, jclass clazz, jdoubleArray inputReal, jboolean DIRECT) {
        jsize length = env->GetArrayLength(inputReal);
        double* samplesDouble = ChordProcessor::fft(env->GetDoubleArrayElements(inputReal, 0), length, DIRECT);
        jdoubleArray output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length, samplesDouble);
        delete[] samplesDouble;
        return output;
    }

    jdoubleArray Java_com_example_GuitAR_services_ChordRecognitionService_bandPassFilter(JNIEnv *env, jclass clazz,
                                                                            jdoubleArray samples, jfloat low_cut,
                                                                            jfloat high_cut, jfloat sr,
                                                                            jfloat buffer_size) {
        jsize length = env->GetArrayLength(samples);
        jdoubleArray output = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(output, 0, length,
                                  ChordProcessor::bandPassFilter(
                                          env->GetDoubleArrayElements(samples, 0),
                                          low_cut, high_cut, sr, buffer_size));
        return output;
    }
}