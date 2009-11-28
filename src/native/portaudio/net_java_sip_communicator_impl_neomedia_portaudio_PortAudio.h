/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_java_sip_communicator_impl_neomedia_portaudio_PortAudio */

#ifndef _Included_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
#define _Included_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
#ifdef __cplusplus
extern "C" {
#endif
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_FRAMES_PER_BUFFER_UNSPECIFIED
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_FRAMES_PER_BUFFER_UNSPECIFIED 0LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT16
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT16 8LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_FLOAT32
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_FLOAT32 1LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT32
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT32 2LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT24
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT24 4LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT8
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_INT8 16LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_UINT8
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_SAMPLE_FORMAT_UINT8 32LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_NO_FLAG
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_NO_FLAG 0LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_CLIP_OFF
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_CLIP_OFF 1LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_DITHER_OFF
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_DITHER_OFF 2LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_NEVER_DROP_INPUT
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_NEVER_DROP_INPUT 4LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_PRIME_OUTPUT_BUFFERS_USING_STREAM_CALLBACK
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_PRIME_OUTPUT_BUFFERS_USING_STREAM_CALLBACK 8LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_PLATFORM_SPECIFIC_FLAGS
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_STREAM_FLAGS_PLATFORM_SPECIFIC_FLAGS -65536LL
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_UNSEPCIFIED
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_UNSEPCIFIED 0.0
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_HIGH
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_HIGH -1.0
#undef net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_LOW
#define net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_LATENCY_LOW -2.0
/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    setEchoCancelParams
 * Signature: (JJZZII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_setEchoCancelParams
  (JNIEnv *, jclass, jlong, jlong, jboolean, jboolean, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetDefaultInputDevice
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetDefaultInputDevice
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetDefaultOutputDevice
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetDefaultOutputDevice
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_CloseStream
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1CloseStream
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_AbortStream
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1AbortStream
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetDeviceCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetDeviceCount
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetDeviceInfo
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetDeviceInfo
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_Initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1Initialize
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_OpenStream
 * Signature: (JJDJJLnet/java/sip/communicator/impl/neomedia/portaudio/PortAudioStreamCallback;)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1OpenStream
  (JNIEnv *, jclass, jlong, jlong, jdouble, jlong, jlong, jobject);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_StartStream
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1StartStream
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_StopStream
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1StopStream
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_WriteStream
 * Signature: (J[BJ)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1WriteStream
  (JNIEnv *, jclass, jlong, jbyteArray, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_ReadStream
 * Signature: (J[BJ)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1ReadStream
  (JNIEnv *, jclass, jlong, jbyteArray, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetStreamReadAvailable
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetStreamReadAvailable
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetStreamWriteAvailable
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetStreamWriteAvailable
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetSampleSize
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetSampleSize
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_IsFormatSupported
 * Signature: (JJD)Z
 */
JNIEXPORT jboolean JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1IsFormatSupported
  (JNIEnv *, jclass, jlong, jlong, jdouble);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getMaxInputChannels
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getMaxInputChannels
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getMaxOutputChannels
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getMaxOutputChannels
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getName
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getName
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getDefaultSampleRate
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getDefaultSampleRate
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getHostApi
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getHostApi
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getDefaultLowInputLatency
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getDefaultLowInputLatency
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getDefaultLowOutputLatency
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getDefaultLowOutputLatency
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getDefaultHighInputLatency
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getDefaultHighInputLatency
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaDeviceInfo_getDefaultHighOutputLatency
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaDeviceInfo_1getDefaultHighOutputLatency
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    Pa_GetHostApiInfo
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_Pa_1GetHostApiInfo
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaHostApiInfo_GetType
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaHostApiInfo_1GetType
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaHostApiInfo_GetName
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaHostApiInfo_1GetName
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaHostApiInfo_GetDeviceCount
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaHostApiInfo_1GetDeviceCount
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaHostApiInfo_GetDefaultInputDevice
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaHostApiInfo_1GetDefaultInputDevice
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaHostApiInfo_GetDefaultOutputDevice
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaHostApiInfo_1GetDefaultOutputDevice
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_portaudio_PortAudio
 * Method:    PaStreamParameters_new
 * Signature: (IIJD)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_portaudio_PortAudio_PaStreamParameters_1new
  (JNIEnv *, jclass, jint, jint, jlong, jdouble);

#ifdef __cplusplus
}
#endif
#endif