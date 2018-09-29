package com.rarepebble.audionotifier

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import java.io.File

private const val sampleRate = 44100
private const val chunkSeconds = 1
private const val chunkFrames = sampleRate * chunkSeconds

class RecorderService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log("start")
        startForeground(1, Notification())
        startRecording()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("destroyed")
    }

}

private fun startRecording() {
    val bytesPerFrame = 2
    val encoding = AudioFormat.ENCODING_PCM_16BIT
    val minBuffBytes = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, encoding)
    if (chunkFrames < minBuffBytes / bytesPerFrame) throw UnsupportedOperationException("chunk too small")

    val buffBytes = chunkFrames * bytesPerFrame

    val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            encoding,
            buffBytes
    )

    recorder.setPositionNotificationPeriod(chunkFrames)
    recorder.setRecordPositionUpdateListener(listener)
    recorder.startRecording()
}

private val listener = object : AudioRecord.OnRecordPositionUpdateListener {
    private val buffer = ShortArray(chunkFrames)
    private val byteBuffer = ByteArray(chunkFrames*2)
    private val file = File("/sdcard/Download/audio.raw")

    override fun onMarkerReached(recorder: AudioRecord?) {}

    override fun onPeriodicNotification(recorder: AudioRecord) {
//        val numRead = recorder.read(buffer, 0, chunkFrames)
//        val rms = Math.sqrt(
//                buffer.map { (it * it).toDouble() }.sum()
//        )
        val numRead = recorder.read(byteBuffer, 0, chunkFrames*2)
        file.appendBytes(byteBuffer)
        log("period: numRead $numRead")
    }

}