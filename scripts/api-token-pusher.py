import os
robotKey = input("API Key=")
channelKey = input ("Channel=")
myCmd = "adb shell am start -n tv.remo.android.controller/.activities.SplashScreen --es 'ApiKey' '%s' --es 'ChannelId' '%s'" % (robotKey, channelKey)
os.system(myCmd)