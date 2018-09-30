package com.rarepebble.audionotifier

class FFT(private var n: Int) {
    private var m: Int = 0

    // Lookup tables. Only need to recompute when size of FFT changes.
    private val cos = DoubleArray(n / 2)
    private val sin = DoubleArray(n / 2)

    init {
        this.m = (Math.log(n.toDouble()) / Math.log(2.0)).toInt()

        // Make sure n is a power of 2
        if (n != 1 shl m)
            throw RuntimeException("FFT length must be power of 2")

        for (i in 0 until n / 2) {
            cos[i] = Math.cos(-2.0 * Math.PI * i.toDouble() / n)
            sin[i] = Math.sin(-2.0 * Math.PI * i.toDouble() / n)
        }

    }

    fun ifft(x: DoubleArray, y: DoubleArray) {
        y.forEachIndexed{ i, v -> y[i] = -v }
        fft(x, y)
        y.forEachIndexed{ i, v -> y[i] = -v }
    }

    fun fft(x: DoubleArray, y: DoubleArray) {
        var i: Int
        var j: Int
        var k: Int
        var n1: Int
        var n2: Int
        var a: Int
        var c: Double
        var s: Double
        var t1: Double
        var t2: Double

        // Bit-reverse
        j = 0
        n2 = n / 2
        i = 1
        while (i < n - 1) {
            n1 = n2
            while (j >= n1) {
                j = j - n1
                n1 = n1 / 2
            }
            j = j + n1

            if (i < j) {
                t1 = x[i]
                x[i] = x[j]
                x[j] = t1
                t1 = y[i]
                y[i] = y[j]
                y[j] = t1
            }
            i++
        }

        // FFT
        n1 = 0
        n2 = 1

        i = 0
        while (i < m) {
            n1 = n2
            n2 = n2 + n2
            a = 0

            j = 0
            while (j < n1) {
                c = cos[a]
                s = sin[a]
                a += 1 shl m - i - 1

                k = j
                while (k < n) {
                    t1 = c * x[k + n1] - s * y[k + n1]
                    t2 = s * x[k + n1] + c * y[k + n1]
                    x[k + n1] = x[k] - t1
                    y[k + n1] = y[k] - t2
                    x[k] = x[k] + t1
                    y[k] = y[k] + t2
                    k = k + n2
                }
                ++j
            }
            ++i
        }
    }
}