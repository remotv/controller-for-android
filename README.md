[![Maintainability](https://api.codeclimate.com/v1/badges/ff37faef91984f7cf420/maintainability)](https://codeclimate.com/github/remotv/controller-for-android/maintainability) [![Actions](https://github.com/remotv/controller-for-android/workflows/Android%20CI/badge.svg)]() [![Deploy build](https://github.com/remotv/controller-for-android/workflows/Deploy%20build/badge.svg)]()


# controller-for-android
remo.tv open source software for pairing your Android powered robot with our site. https://remo.tv

Currently under development. Not everything may work at the moment

# WIKI

For setup guide and button templates, please check out the [WIKI](https://github.com/remotv/controller-for-android/wiki)

# Development

## Required software

### Android Studio

- Version 3.5 or higher. May not be buildable on lower versions
- Android SDK (should be installed with Android Studio)
- Java 7 or 8 JDK (might be installed with Android Studio)

## Branches

### master
 
The most stable code in the repo. Can be used for testing and is known to work

### develop

The latest code, mostly stable, but might have issues. Sometimes this code may not be buildable

## Device Limitations

- Setup to be capable of running on Android 4.1 (API 16) or higher. Some devices may run into issues, so feel free to report them

- Android Things not tested

- Raspberry PI running regular Android not tested


# Supported or broken devices

Feel free to add your device to this list if you have tested it via a pull request

## Broken devices:

-
-

## Verified functional devices:

- Casio G'zOne CA-201L (4.1.2 JellyBean). Tested with 512kbps bitrate and bluetooth. Might not support USB OTG

- ZTE Speed (4.4 Kitkat), might not be fast enough on high bitrates, some weird bluetooth issues possible (https://github.com/btelman96/runmyrobot_android/issues/45)

- Motorola Moto Z (8.0.0 Oreo)

- Galaxy S4 (5.0.1 Lollipop)

- Pixel 2 XL (9 Pie)

- Samsung Galaxy J5

- Samsung Galaxy M20 (9 Pie)

- Samsung Galaxy A90 (9 Pie)


This software uses code of <a href=http://ffmpeg.org>FFmpeg</a> licensed under the <a href=http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>LGPLv2.1</a> and its source can be downloaded <a href=https://github.com/btelman96/ffmpeg-android>here</a>
Note: the ffmpeg binaries are licensed under LGPL3.0, but the rest of the code is Apache 2.0
