package com.rarepebble.audionotifier

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.content.Intent
import android.util.Log
import android.widget.TextView
import java.text.DateFormat
import java.util.*


class MainActivity : Activity() {

    val logView by lazy {findViewById<TextView>(R.id.log)}
    val statusView by lazy {findViewById<TextView>(R.id.status)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent().setClass(this, DetectorService::class.java)
        startService(intent)
        bindService(intent, object: ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
            }

            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                if (binder is DetectorBinder) {
                    binder.service.setCallbacks(this@MainActivity::onStatus, this@MainActivity::onLog)
                }
            }

        }, Context.BIND_AUTO_CREATE)

    }

    private fun onLog(s: String) {
        runOnUiThread {
            Log.i("AndroidAudioNotifier", s)
            val time = DateFormat.getDateTimeInstance().format(Date())
            logView.append("${time} ${s}\n")
        }
    }

    private fun onStatus(s: String) {
        runOnUiThread{
            statusView.setText(s)
        }
    }
}
