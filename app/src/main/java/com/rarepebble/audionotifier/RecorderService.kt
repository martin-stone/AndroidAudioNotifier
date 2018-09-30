package com.rarepebble.audionotifier

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder

private const val sampleRate = 44100
private const val bufferSamples = 32768
private val fft = FFT(bufferSamples)

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

}

private fun startRecording() {
    val bytesPerFrame = 2
    val encoding = AudioFormat.ENCODING_PCM_16BIT
    val minBuffBytes = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, encoding)
    if (bufferSamples < minBuffBytes / bytesPerFrame) throw UnsupportedOperationException("chunk too small")

    val buffBytes = bufferSamples * bytesPerFrame

    val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            encoding,
            buffBytes
    )

    recorder.setPositionNotificationPeriod(bufferSamples)
    recorder.setRecordPositionUpdateListener(listener)
    recorder.startRecording()
}

private val listener = object : AudioRecord.OnRecordPositionUpdateListener {
    private val buffer = ShortArray(bufferSamples)
    //private val byteBuffer = ByteArray(bufferSamples*2)
    //private val file = File("/sdcard/Download/audio.raw")

    override fun onMarkerReached(recorder: AudioRecord?) {}

    override fun onPeriodicNotification(recorder: AudioRecord) {
        val numRead = recorder.read(buffer, 0, bufferSamples)
        val f = fundamentalFreq(buffer)
        log("freq: $f")
    }
}

private val real = DoubleArray(bufferSamples)
private val imag = DoubleArray(bufferSamples)


private fun fundamentalFreq(buffer: ShortArray): Double {
    buffer.forEachIndexed { i, v -> real[i] = v.toDouble()}
    imag.fill(0.0)
    fft.fft(real, imag)
    val peakIndex = (0..bufferSamples/2).maxBy { i ->
        val re = real[i]
        val im = imag[i]
        re*re + im*im
    }!!
    return peakIndex.toDouble() * sampleRate.toDouble() / bufferSamples
}

//private fun fundamentalFreq(buffer: ShortArray): Double {
//    buffer.forEachIndexed { i, v -> real[i] = v.toDouble()}
//    imag.fill(0.0)
//    // autocorrelate WRONG: NEED TO PAD https://stackoverflow.com/a/3950398
//    fft.fft(real, imag)
//    for (i in 0..bufferSamples-1) {
//        // multiply by complex conjugage
//        val re = real[i]
//        val im = imag[i]
//        real[i] = re*re + im*im
//        imag[i] = 0.0
//    }
//    fft.ifft(real, imag)
//
//    // Find peak periodicity (skip dc)
//    val peakIndex = (1..bufferSamples-1).maxBy { i ->
//        val re = real[i]
//        val im = imag[i]
//        re*re + im*im
//    }!!
//    return sampleRate.toDouble() / peakIndex
//}


//private fun fundamentalFreq(buffer: ShortArray): Double {
//    // Autocorrelate, find fundamental.
//    val (peakIndex, peakValue) = buffer.withIndex().maxBy { offset ->
//        val offsetValue = offset.value.toDouble()
//        if (offset.index == 0)
//            Double.MIN_VALUE // ignore DC and avoid / by 0
//        else
//            buffer.sumByDouble { it.toDouble() * offsetValue }
//    }!!
//    log("val $peakIndex, $peakValue")
//    return sampleRate.toDouble() / peakIndex
//}