//
// Created by michael on 12/19/21.
//

#ifndef SMEMAPPS_BITMAP_CONTAINER_H
#define SMEMAPPS_BITMAP_CONTAINER_H

#include <stdint.h>
#include <jni.h>

typedef struct bmp_container
{
    uint16_t width;
    uint16_t height;
    uint8_t format;
    void* buf;
} bmp_container_t;

#endif //SMEMAPPS_BITMAP_CONTAINER_H
