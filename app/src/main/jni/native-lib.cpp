#include <jni.h>
#include "ChordProcessor.h"

extern "C"{
    ChordProcessor * cd = new ChordProcessor();

    jintArray Java_com_example_pitchdetection_JNIParser_chordDetectionJni(JNIEnv *env, jclass clazz,
                                                                      jdoubleArray samples) {
        jintArray salida = env->NewIntArray(2);
        env->SetIntArrayRegion(salida,0,2, cd->chordDetection(env->GetDoubleArrayElements(samples,0)));
        return salida;
    }
    jdoubleArray Java_com_example_pitchdetection_JNIParser_windowJni(JNIEnv *env, jclass clazz,
                                                    jdoubleArray samples) {
        jsize length = env->GetArrayLength(samples);
        jdoubleArray salida = env->NewDoubleArray(length);
        env->SetDoubleArrayRegion(salida, 0, length, cd->window(env->GetDoubleArrayElements(samples, 0), length));
        return salida;
    }
}

