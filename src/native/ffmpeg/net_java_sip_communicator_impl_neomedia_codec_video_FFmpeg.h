/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg */

#ifndef _Included_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
#define _Included_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    av_free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_av_1free
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    av_malloc
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_av_1malloc
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    av_register_all
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_av_1register_1all
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_alloc_context
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1alloc_1context
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_alloc_frame
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1alloc_1frame
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_close
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1close
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_decode_video
 * Signature: (JJ[Z[BI)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1decode_1video
  (JNIEnv *, jclass, jlong, jlong, jbooleanArray, jbyteArray, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_encode_video
 * Signature: (J[BIJ)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1encode_1video
  (JNIEnv *, jclass, jlong, jbyteArray, jint, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_find_decoder
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1find_1decoder
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_find_encoder
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1find_1encoder
  (JNIEnv *, jclass, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1init
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodec_open
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodec_1open
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_add_flags
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1add_1flags
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_add_partitions
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1add_1partitions
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_get_height
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1get_1height
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_get_pix_fmt
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1get_1pix_1fmt
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_get_width
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1get_1width
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_b_frame_strategy
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1b_1frame_1strategy
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_bit_rate
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1bit_1rate
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_bit_rate_tolerance
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1bit_1rate_1tolerance
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_chromaoffset
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1chromaoffset
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_crf
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1crf
  (JNIEnv *, jclass, jlong, jfloat);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_deblockbeta
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1deblockbeta
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_gop_size
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1gop_1size
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_i_quant_factor
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1i_1quant_1factor
  (JNIEnv *, jclass, jlong, jfloat);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_max_b_frames
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1max_1b_1frames
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_mb_decision
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1mb_1decision
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_me_cmp
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1me_1cmp
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_me_method
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1me_1method
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_me_range
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1me_1range
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_me_subpel_quality
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1me_1subpel_1quality
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_pix_fmt
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1pix_1fmt
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_qcompress
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1qcompress
  (JNIEnv *, jclass, jlong, jfloat);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_quantizer
 * Signature: (JIII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1quantizer
  (JNIEnv *, jclass, jlong, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_rc_buffer_size
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1rc_1buffer_1size
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_rc_eq
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1rc_1eq
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_rc_max_rate
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1rc_1max_1rate
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_refs
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1refs
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_rtp_payload_size
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1rtp_1payload_1size
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_sample_aspect_ratio
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1sample_1aspect_1ratio
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_scenechange_threshold
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1scenechange_1threshold
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_size
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1size
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_thread_count
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1thread_1count
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_ticks_per_frame
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1ticks_1per_1frame
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_time_base
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1time_1base
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_trellis
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1trellis
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avcodeccontext_set_workaround_bugs
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avcodeccontext_1set_1workaround_1bugs
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avframe_get_pts
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avframe_1get_1pts
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avframe_set_data
 * Signature: (JJJJ)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avframe_1set_1data
  (JNIEnv *, jclass, jlong, jlong, jlong, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avframe_set_key_frame
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avframe_1set_1key_1frame
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avframe_set_linesize
 * Signature: (JIII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avframe_1set_1linesize
  (JNIEnv *, jclass, jlong, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avpicture_fill
 * Signature: (JJIII)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avpicture_1fill
  (JNIEnv *, jclass, jlong, jlong, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avpicture_get_data0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avpicture_1get_1data0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    avpicture_get_size
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_avpicture_1get_1size
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    memcpy
 * Signature: ([IIIJ)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_memcpy___3IIIJ
  (JNIEnv *, jclass, jintArray, jint, jint, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    memcpy
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_memcpy__J_3BII
  (JNIEnv *, jclass, jlong, jbyteArray, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_BGR32
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1BGR32
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_BGR32_1
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1BGR32_11
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_RGB24
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1RGB24
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_RGB32
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1RGB32
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_RGB32_1
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1RGB32_11
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    PIX_FMT_YUV420P
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_PIX_1FMT_1YUV420P
  (JNIEnv *, jclass);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    sws_freeContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_sws_1freeContext
  (JNIEnv *, jclass, jlong);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    sws_getCachedContext
 * Signature: (JIIIIIII)J
 */
JNIEXPORT jlong JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_sws_1getCachedContext
  (JNIEnv *, jclass, jlong, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    sws_scale
 * Signature: (JJIILjava/lang/Object;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_sws_1scale__JJIILjava_lang_Object_2III
  (JNIEnv *, jclass, jlong, jlong, jint, jint, jobject, jint, jint, jint);

/*
 * Class:     net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg
 * Method:    sws_scale
 * Signature: (JLjava/lang/Object;IIIIILjava/lang/Object;III)I
 */
JNIEXPORT jint JNICALL Java_net_java_sip_communicator_impl_neomedia_codec_video_FFmpeg_sws_1scale__JLjava_lang_Object_2IIIIILjava_lang_Object_2III
  (JNIEnv *, jclass, jlong, jobject, jint, jint, jint, jint, jint, jobject, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
