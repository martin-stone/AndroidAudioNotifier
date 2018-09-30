package com.rarepebble.audionotifier

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

class DetectorService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, Notification())

        val detector = AlarmDetector(this::onDetected)
        startDetectingFrequencies(detector::onFrequency)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onDetected() {
    }

}


