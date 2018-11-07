package com.rarepebble.audionotifier

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.content.Intent
import android.util.Log
import android.widget.TextView


class MainActivity : Activity() {

    private val logView by lazy {findViewById<TextView>(R.id.log)}
    private val statusView by lazy {findViewById<TextView>(R.id.status)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent().setClass(this, DetectorService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(serviceConnection)
    }

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
        }

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            if (binder is DetectorBinder) {
                binder.service.setCallbacks(this@MainActivity::onStatus, this@MainActivity::onLog)
            }
        }

    }

    private fun onLog(s: String, logLines: List<String>) {
        runOnUiThread {
            Log.i("AndroidAudioNotifier", s)
            logView.setText(logLines.joinToString("\n"))
        }
    }

    private fun onStatus(s: String) {
        runOnUiThread{
            statusView.setText(s)
        }
    }
}
