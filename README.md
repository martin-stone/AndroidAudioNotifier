# Android Audio Notifier

## AKA Laundry Bot

This Android app runs on my Samsung GS1 and emails me when my washing machine beeps to indicate that it's finished.
It's not very configurable: Beep detection settings are configured in *AlarmDetector.kt* code.
Email is configured via Config.kt (not committed -- Add your own if you want to build it).

Config.kt

    package com.rarepebble.audionotifier
    
    const val SMTP_HOST = ""
    const val SMTP_PORT = ""
    const val SMTP_USERNAME = ""
    const val SMTP_PASSWORD = ""
    
    const val EMAIL_FROM = ""
    const val EMAIL_TO = ""
    const val EMAIL_SUBJECT = "The wash just finished"
    const val EMAIL_TEXT = "\n"
   
Memory allocations have been minimised and it can run for a very long time without problems, 
however sometimes it does stop working and the phone needs a restart. 
The app is tagged as a homescreen app, so you can configure it to start automatically on boot 
and display the log continuously.
