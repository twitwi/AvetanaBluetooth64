/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_avetana_bluetooth_stack_BlueZ */

#ifndef _Included_de_avetana_bluetooth_stack_BlueZ
#define _Included_de_avetana_bluetooth_stack_BlueZ
#ifdef __cplusplus
extern "C" {
#endif
/* Inaccessible static: myFactory */
/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciOpenDevice
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciOpenDevice
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciCloseDevice
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciCloseDevice
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    macScan
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_macScan
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciInquiry
 * Signature: (IIIJ)Lde/avetana/bluetooth/hci/HCIInquiryResult;
 */
JNIEXPORT jobject JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciInquiry
  (JNIEnv *, jclass, jint, jint, jint, jlong);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciDevBTAddress
 * Signature: (I)Lde/avetana/bluetooth/util/BTAddress;
 */
JNIEXPORT jobject JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciDevBTAddress
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciDeviceID
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciDeviceID
  (JNIEnv *, jclass, jstring);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciLocalName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciLocalName
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    hciRemoteName
 * Signature: (ILjava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_avetana_bluetooth_stack_BlueZ_hciRemoteName
  (JNIEnv *, jclass, jint, jstring, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    openRFCommNative
 * Signature: (Ljava/lang/String;IZZZ)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_openRFCommNative
  (JNIEnv *, jclass, jstring, jint, jboolean, jboolean, jboolean);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    openL2CAPNative
 * Signature: (Ljava/lang/String;IZZZII)Lde/avetana/bluetooth/l2cap/L2CAPConnParam;
 */
JNIEXPORT jobject JNICALL Java_de_avetana_bluetooth_stack_BlueZ_openL2CAPNative
  (JNIEnv *, jclass, jstring, jint, jboolean, jboolean, jboolean, jint, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    closeConnection
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_closeConnection
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    readBytes
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_readBytes
  (JNIEnv *, jclass, jint, jbyteArray, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    writeBytes
 * Signature: (I[BII)V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_writeBytes
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    listService
 * Signature: (Ljava/lang/String;[S[I)V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_listService
  (JNIEnv *, jclass, jstring, jshortArray, jintArray);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    createService
 * Signature: (Lde/avetana/bluetooth/sdp/LocalServiceRecord;)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_createService
  (JNIEnv *, jclass, jobject);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    updateService
 * Signature: ([BIJ)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_updateService
  (JNIEnv *, jclass, jbyteArray, jint, jlong);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    registerService
 * Signature: (IIZZZ)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_registerService
  (JNIEnv *, jclass, jint, jint, jboolean, jboolean, jboolean);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    registerL2CAPService
 * Signature: (IIZZZII)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_registerL2CAPService
  (JNIEnv *, jclass, jint, jint, jboolean, jboolean, jboolean, jint, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    disposeLocalRecord
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_avetana_bluetooth_stack_BlueZ_disposeLocalRecord
  (JNIEnv *, jclass, jlong);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    getAccessMode
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_getAccessMode
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    setAccessMode
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_setAccessMode
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    getDeviceClass
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_getDeviceClass
  (JNIEnv *, jclass, jint);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    isMasterSwitchAllowed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_avetana_bluetooth_stack_BlueZ_isMasterSwitchAllowed
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    getMaxConnectedDevices
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_getMaxConnectedDevices
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    inquiryScanAndConAllowed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_avetana_bluetooth_stack_BlueZ_inquiryScanAndConAllowed
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    inquiryAndConAllowed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_avetana_bluetooth_stack_BlueZ_inquiryAndConAllowed
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    pageScanAndConAllowed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_avetana_bluetooth_stack_BlueZ_pageScanAndConAllowed
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    pageAndConnAllowed
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_de_avetana_bluetooth_stack_BlueZ_pageAndConnAllowed
  (JNIEnv *, jclass);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    authenticate
 * Signature: (ILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_authenticate
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    encrypt
 * Signature: (ILjava/lang/String;Z)I
 */
JNIEXPORT jint JNICALL Java_de_avetana_bluetooth_stack_BlueZ_encrypt
  (JNIEnv *, jclass, jint, jstring, jboolean);

/*
 * Class:     de_avetana_bluetooth_stack_BlueZ
 * Method:    connectionOptions
 * Signature: (ILjava/lang/String;)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_de_avetana_bluetooth_stack_BlueZ_connectionOptions
  (JNIEnv *, jclass, jint, jstring);

#ifdef __cplusplus
}
#endif
#endif
