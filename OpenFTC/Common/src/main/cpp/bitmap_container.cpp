//
// Created by michael on 12/19/21.
//

#include <stdint.h>
#include "bitmap_container.h"
#include <android/bitmap.h>
#include <android/log.h>
#include <string.h>

extern "C" JNIEXPORT jlong JNICALL
Java_org_firstinspires_ftc_smem_common_BitmapContainer_n_1BitmapContainer(JNIEnv *env, jclass clazz, jint width, jint height, jint format, jlong ptrBuf)
{
    bmp_container_t* container = new bmp_container_t();
    container->width = width;
    container->height = height;
    container->format = format;
    container->buf = (void*) ptrBuf;

    return (jlong) container;
}

extern "C" JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_smem_common_BitmapContainer_n_1del_1BitmapContainer(JNIEnv *env, jclass clazz, jlong ptrBmpContainer)
{
    bmp_container_t* container = (bmp_container_t*) ptrBmpContainer;
    delete container;
}

extern "C" JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_smem_common_BitmapContainer_fillAndroidBmpFromContainer(JNIEnv *env, jclass clazz, jobject jBmpObj, jlong ptrBmpContainer)
{
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, jBmpObj, &info);
    bmp_container_t* container = (bmp_container_t*) ptrBmpContainer;
    void* androidBmpPixels;

    if(container->width != info.width || container->height != info.height)
    {
        env->ThrowNew(
                env->FindClass("java/lang/RuntimeException"),
                "container->width != info.width || container->height != info.height");
        return;
    }

    if(AndroidBitmap_lockPixels(env, jBmpObj, &androidBmpPixels) < 0)
    {
        env->ThrowNew(
                env->FindClass("java/lang/RuntimeException"),
                "Failed to lock pixels");
        return;
    }

    memcpy(androidBmpPixels, container->buf, info.width*info.height*4);

    AndroidBitmap_unlockPixels(env, jBmpObj);
}

extern "C" JNIEXPORT void JNICALL
Java_org_firstinspires_ftc_smem_common_BitmapContainer_copyAndroidBitmap(JNIEnv *env, jclass clazz, jobject src, jobject dst)
{
    void* srcPixels;
    void* dstPixels;

    AndroidBitmapInfo info_src;
    AndroidBitmapInfo info_dst;

    AndroidBitmap_getInfo(env, src, &info_src);
    AndroidBitmap_getInfo(env, dst, &info_dst);

    AndroidBitmap_lockPixels(env, src, &srcPixels);
    AndroidBitmap_lockPixels(env, dst, &dstPixels);

    memcpy(dstPixels, srcPixels, info_dst.width*info_dst.height*4);

    AndroidBitmap_unlockPixels(env, src);
    AndroidBitmap_unlockPixels(env, dst);
}