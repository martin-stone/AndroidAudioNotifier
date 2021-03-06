package com.rarepebble.audionotifier

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioRecord
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import java.text.DateFormat
import java.util.*


class DetectorService : Service() {

    private val logLines = mutableListOf<String>()
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var recorder: AudioRecord // Must hold ref to avoid GC stopping recording.
    private var onStatus = {s:String -> defaultLog("status: ${s}", logLines)}
    private var onLog = {s:String, logLines: List<String> -> defaultLog(s, logLines)}


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, Notification())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        log("Creating.")
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AndroidAudioNotifier:WakeLock").apply {
                acquire()
                log("Wake lock aquired.")
            }
        }
        val detector = AlarmDetector(this::onDetected, this::log, this::showStatus)
        recorder = startDetectingFrequencies(detector::onFrequency)
    }

    override fun onBind(intent: Intent): IBinder {
        return DetectorBinder(this)
    }

    override fun onDestroy() {
        log("Destroying.")
        wakeLock.release()
        super.onDestroy()
    }

    fun log(s: String) {
        val time = DateFormat.getDateTimeInstance().format(Date())
        synchronized(this) {
            logLines.add("${time} ${s}")
            val maxLogLines = 500
            if (logLines.size > maxLogLines) {
                logLines.removeAt(0)
            }
            onLog(s, logLines)
        }
    }

    fun showStatus(s: String) = onStatus(s)

    private fun onDetected() {
        sendEmailAsync(
                SMTP_HOST, SMTP_PORT,
                SMTP_USERNAME, SMTP_PASSWORD,
                EMAIL_FROM, EMAIL_TO,
                EMAIL_SUBJECT, EMAIL_TEXT,
                this::log
        )
    }

    fun setCallbacks(onStatus: (String) -> Unit, onLog: (String, List<String>) -> Unit) {
        this.onStatus = onStatus
        this.onLog = onLog
        onLog("", logLines)
    }
}


class DetectorBinder(val service: DetectorService) : Binder()


private fun defaultLog(s: String, logLines: List<String>) {
    Log.i("AndroidAudioNotifier", s)
}