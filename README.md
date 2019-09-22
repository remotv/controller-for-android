[![Maintainability](https://api.codeclimate.com/v1/badges/ff37faef91984f7cf420/maintainability)](https://codeclimate.com/github/remotv/controller-for-android/maintainability) [![Actions](https://github.com/remotv/controller-for-android/workflows/Android%20CI/badge.svg)]() [![Deploy build](https://github.com/remotv/controller-for-android/workflows/Deploy%20build/badge.svg)]()


# controller-for-android
remo.tv open source software for pairing your Android powered robot with our site. http://dev.remo.tv:5000/

Currently under development. Not everything works at the moment, such as volume boost or microphone bitrate

# Development

## Required software

### Android Studio

- Version 3.5 or higher. May not be buildable on lower versions
- Android SDK (should be installed with Android Studio)
- Java 7 or 8 JDK (might be installed with Android Studio)

## Branches

### master
 
The most stable code in the repo. Can be used for testing and is known to work

### devel

The latest code, mostly stable, but might have issues. Sometimes this code may not be buildable

## Device Limitations

- Setup to be capable of running on Android 4.1 (API 16) or higher. Some devices may run into issues, so feel free to report them

- Android Things not tested

- Raspberry PI running regular Android not tested

# Setup

## Robot and Camera Ids

When the app is started, and permissions have been accepted (API > 23), a setup screen will pop up
with settings that can be changed. Going into the connection section will bring you to an area where the API Key and channel ID can be pasted in.

### User Configurable Settings

- API Key

- Channel Id

- Robot Hardware switch

- Camera Hardware switch

- Camera settings such as resolution, bitrate, orientation, and front/back camera

- Enable Microphone Toggle

- Text to speech toggle

- Bluetooth device setup

## Running the robot

### To make the robot operational and connected:

 1. If building from source, click build and run to deploy to phone in Android Studio (Play button)
 2. Open App if not opened already
 3. Accept permissions if they pop up
 4. Configure robot settings
 5. Hit POWER - Button will be disabled until fully connected
 6. Robot will now be connected to website. POWER button will be green
 
### Supported Commands ###

#### Table mode ####
Only the owner can use this.
Turns on table top mode, not allowing 'f' and 'b' commands, but allows everything else. Not case sensitive

On: `.table on`

Off: `.table off`

Toggle on and off: `/stationary`

The robot has to handle this command, unless set in the app settings to be handled locally. The option exists for both since some hardware is not programmable, such as a sabertooth serial controller. In the case that the app controls it, the list of restricted controls are configurable as well

#### Disable All Control ####
Only the owner can use this.
Disables all commands, even for the owner.

Disable: '.motors off'
Enable: '.motors on'

#### Exclusive Control ####

`/xcontrol user 60`: give user user control of robot for 60 seconds
`/xcontrol ~ 60` (button): give user who pressed the button exclusive control for 60 seconds
`/xcontrol user`: user user gets exclusive control indefinitely or until the robot reboots
`/xcontrol off`: turn off exclusive control

#### Dev mode ####
Only works if the owner is added to settings in the connection settings

/devmode on: turn on devmode
/devmode off: turn off devmode
 
### Ways to stop the robot:
 
 - A notification will appear that states that the app is active. Use the "Terminate App" Button to 
 kill the app from anywhere
 
 - Hit the app's POWER button again when it is green to disable
 
 - Swipe app away from recents
 
 - typing `/estop` in the chat. Will send a `stop` command to the hardware then kill the app forcibly

### App Permissions ###

#### Location ####

Needed for bluetooth device scanning. Not requested or used otherwise

#### Camera ####

Needed to stream the camera. Only requested if camera is enabled in settings

#### Microphone ####

Needed to stream the microphone. Only requested if the mic is enabled in settings

### Troubleshooting issues (TODO)

#### Flickering Camera and Microphone indicators

Reload the robot page on Remo.TV (May be unnecessary)

Also check that the api key, and channel id match with the site.

#### Most indicators immediately go red

Check the phone's internet connection

#### Most indicators go red after being yellow for some time

- Check the phone's internet connection.
 
- Connection may be too slow or connected to a WiFi router with no internet.

- Also could potentially be a site issue, but most of the time it would be internet related

#### Robot turns off after some time if I turn the screen off ####
This only applies to Android 6.0 and above

- Go to the app settings and turn battery optimization off for this app (There is a shortcut for this in display settings in Remo app)

## Adding separate components

TODO

### Devices supported
 
 - SaberTooth Motor Controllers (Simplified Serial), 9600 BAUD
 - Arduino via raw commands (f, b, l, r, stop, etc), 9600 BAUD, USB or Bluetooth Classic
 - Any device that has the same protocol as SaberTooth
 - Lego Mindstorms NXT (JoystickDriver or equivalent. Most commonly used with older FIRST Tech Challenge robots that ran RobotC and Labview)
 
### Connection Options

 - Bluetooth Classic (less than 4.0 guaranteed), HC04 would work. Please pair the bluetooth device in settings first, then setup in this app
 - USB Serial (Not working on Android Things 1.0.3 rpi3) (https://github.com/felHR85/UsbSerial#devices-supported)

## Error reporting

Errors do not get reported, but will show a notification on the device if a component fails

# Some known issues

- Battery optimization has to be disabled if OS version is 6.0 or above if you want to turn the screen off on the phone.

- Currently no code to use a USB webcam. Also currently not sure if the Raspberry Pi with camera functions as is

- BluetoothClassic currently not hooked up to handle input from the connected device. It can output to it just fine

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

- Samsung Galaxy J5 (tested with 768x432, 512kb/s)


This software uses code of <a href=http://ffmpeg.org>FFmpeg</a> licensed under the <a href=http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>LGPLv2.1</a> and its source can be downloaded <a href=https://github.com/btelman96/ffmpeg-android-java>here</a>
