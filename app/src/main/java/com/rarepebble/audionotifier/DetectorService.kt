package com.rarepebble.audionotifier

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager

class DetectorService : Service() {

    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, Notification())
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AndroidAudioNotifier").apply {
                acquire()
            }
        }


        val detector = AlarmDetector(this::onDetected)
        startDetectingFrequencies(detector::onFrequency)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.release()
    }


    private fun onDetected() {
    }

}


