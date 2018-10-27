package com.rarepebble.audionotifier

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

private const val sampleRate = 44100
private const val bufferSamples = 32768

private val fft = FFT(bufferSamples)
private val real = DoubleArray(bufferSamples)
private val imag = DoubleArray(bufferSamples)

internal fun startDetectingFrequencies(onFrequency: (Double) -> Unit ): AudioRecord {
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
    recorder.setRecordPositionUpdateListener(Listener(onFrequency))
    recorder.startRecording()
    return recorder
}

private class Listener(private val onFrequency: (Double) -> Unit): AudioRecord.OnRecordPositionUpdateListener {
    private val buffer = ShortArray(bufferSamples)

    override fun onMarkerReached(recorder: AudioRecord?) {}

    override fun onPeriodicNotification(recorder: AudioRecord) {
        recorder.read(buffer, 0, bufferSamples)
        onFrequency(fundamentalFreq(buffer))
    }
}

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

