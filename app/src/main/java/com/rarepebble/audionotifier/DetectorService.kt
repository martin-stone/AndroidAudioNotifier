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
        log("detected")
    }

}


internal class AlarmDetector(
        private val onDetected: () -> Unit,
        private val numHistory: Int = 10,
        private val matchCountThreshold: Int = 2,
        private val beeperFreq: Double = 4000.0, // Hz
        private val freqTolerance: Double = 10.0, // Hz (XXX really depends on sample rate & fft size)
        debouncePeriodMins: Int = 15
        ) {
    private val recent = DoubleArray(numHistory)
    private val debouncePeriodMillisec = debouncePeriodMins * 60 * 1000
    private var nextI = 0
    private var lastNotifyTime = 0L

    fun onFrequency(f: Double) {
        recent[nextI] = f
        nextI = (nextI + 1) % numHistory

        val minFreq = beeperFreq - freqTolerance
        val maxFreq = beeperFreq + freqTolerance
        val detected = recent.count {minFreq < it && it < maxFreq } >= matchCountThreshold

        val notifyAllowed = System.currentTimeMillis() - lastNotifyTime > debouncePeriodMillisec

        if (detected && notifyAllowed) {
            lastNotifyTime = System.currentTimeMillis()
            onDetected()
        }
    }

}
