#include <jni.h>
#include <string>
#include <sys/mman.h>
#include <linux/ashmem.h>
#include <cstddef>
#include "cutils/ashmem.h"
#include <android/log.h>
#include <unistd.h>
#include <errno.h>
#include <unistd.h>

#define TAG "ashmem_jni"

extern "C" JNIEXPORT jint JNICALL
Java_org_openftc_ashmem_AshmemJNI_ashmem_1create_1region(JNIEnv* env, jclass thizz, jstring jName, jint size)
{
    const char *name = env->GetStringUTFChars(jName, NULL);
    int ret = ashmem_create_region(name, size);

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "create region, fd=%d", ret);

    env->ReleaseStringUTFChars(jName, name);
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_org_openftc_ashmem_AshmemJNI_ashmem_1set_1prot_1region(JNIEnv *env, jclass clazz, jint fd, jint prot)
{
    return ashmem_set_prot_region(fd, prot);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_org_openftc_ashmem_AshmemJNI_ashmem_1pin_1region(JNIEnv *env, jclass clazz, jint fd, jint offset, jint len)
{
    return ashmem_pin_region(fd, offset, len);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_org_openftc_ashmem_AshmemJNI_ashmem_1unpin_1region(JNIEnv *env, jclass clazz, jint fd,
                                                        jint offset, jint len) {
    return ashmem_unpin_region(fd, offset, len);
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_openftc_ashmem_AshmemJNI_ashmem_1get_1size_1region(JNIEnv *env, jclass clazz, jint fd) {

    return ashmem_get_size_region(fd);
}

extern "C"
JNIEXPORT jlong JNICALL
Java_org_openftc_ashmem_AshmemJNI_map_1region(JNIEnv *env, jclass clazz, jint fd, jint len)
{

    __android_log_print(ANDROID_LOG_DEBUG, TAG, "trying map, fd=%d", fd);

    uint8_t* map = (uint8_t*) mmap(NULL, len, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);

    if(map == MAP_FAILED)
    {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "MAP_FAILED %d", errno);
    }
    else
    {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "MAP_SUCCESS");

        __android_log_print(ANDROID_LOG_DEBUG, TAG, "MAP: FIRST BYTE: %d", map[0]);

        for(int i = 0; i < 100; i++)
        {
            map[i] = 254;
        }
    }

    return (jlong) map;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_org_openftc_ashmem_AshmemJNI_unmap_1region(JNIEnv *env, jclass clazz, jlong addr, jint len)
{
    return munmap((void*)addr, len);
}

extern "C"
JNIEXPORT void JNICALL
Java_org_openftc_ashmem_AshmemJNI_closeFd(JNIEnv *env, jclass clazz, jint fd)
{
    close(fd);
}